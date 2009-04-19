package net.sourceforge.vrapper.core.tests.cases;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.modes.CommandBasedMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.SimpleRegister;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;

public class NormalModeTests extends CommandTestCase {
	protected Register defaultRegister;

	@Override
	public void setUp() {
		super.setUp();
		mode = new NormalMode(adaptor);
		defaultRegister = new SimpleRegister();
		when(registerManager.getActiveRegister()).thenReturn(defaultRegister);
		when(fileService.isEditable()).thenReturn(true);
	};

	private void assertYanked(ContentType type, String text) {
		assertEquals(type, defaultRegister.getContent().getPayloadType());
		assertEquals(text, defaultRegister.getContent().getText());
	}

	@Test public void testEnteringNormalModeChangesCaret() {
		mode.enterMode();
		assertEquals(CaretType.RECTANGULAR, cursorAndSelection.getCaret());
	}

	@Test public void test_x() {
		checkCommand(forKeySeq("x"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
	}

	@Test public void test_s() {
		checkCommand(forKeySeq("s"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
		verify(adaptor).changeMode(InsertMode.NAME);
	}

	@Test public void test_X() {
		// TODO: behaviour on first character of buffer is stupid and non-compatibile
		checkCommand(forKeySeq("X"),
				"Al",'a'," ma kota",
				"A",'a'," ma kota");
		assertYanked(ContentType.TEXT, "l");
	}

	@Test public void test_dw() {
		checkCommand(forKeySeq("dw"),
			"Ala ",'m',"a kota",
			"Ala ",'k',"ota");
		assertYanked(ContentType.TEXT, "ma ");
	}

	@Test public void test_diw() {
		checkCommand(forKeySeq("diw"),
				"Ala mi",'e',"wa kota",
				"Ala ",' ',"kota");
		assertYanked(ContentType.TEXT, "miewa");
	}

	@Test public void test_daw() {
		checkCommand(forKeySeq("daw"),
				"Ala mi",'e',"wa kota",
				"Ala ",'k',"ota");
		assertYanked(ContentType.TEXT, "miewa ");
	}

	@Test public void test_p() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala czasam",'i'," ma kota");
	}

	@Test public void test_p_empty_clipboard() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, ""));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Al",'a'," ma kota");
	}

	@Test public void test_p_lines() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota\nanother line",
				"Ala ma kota\n",'A',"s to pies Ali\nanother line");
	}

	@Test public void test_p_lines_end_of_buffer() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala ma kota\n",'A',"s to pies Ali\n");
	}

	// TODO: I don't like Vim's p behaviour for P with text -- make compatibility optional and THEN test it

	@Test public void test_P_lines() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("P"),
				"Al",'a'," ma kota",
				"",'A',"s to pies Ali\nAla ma kota");
	}

	@Test public void test_dot() {
		Command dw = new TextOperationTextObjectCommand(new DeleteOperation(), new MotionTextObject(new MoveWordRight()));
		when(registerManager.getLastEdit()).thenReturn(dw);
		checkCommand(forKeySeq(".."),
				"A",'l',"a ma kota i psa",
				"A",'k',"ota i psa");
	}

	@Test public void testThereIsNoRedrawsWhenCommandIsExecuted() {
		Command checkIt = new CountIgnoringNonRepeatableCommand() {
			public void execute(EditorAdaptor editorAdaptor) {
				assertSame(adaptor, editorAdaptor);
				verify(viewportService).setRepaint(false);
			}
		};
		CommandBasedMode normalMode = (CommandBasedMode) mode;
		normalMode.executeCommand(checkIt);
	}

	@Test public void testLastCommandRegistrationWhenThereIsRepetition() {
		Command repetition = mock(Command.class);
		Command cmd = mock(Command.class);
		when(cmd.repetition()).thenReturn(repetition);
		CommandBasedMode normalMode = (CommandBasedMode) mode;
		normalMode.executeCommand(cmd);
		verify(registerManager).setLastEdit(repetition);
	}

	@Test public void testThereIsNoCommandRegistrationWhenThereIsNoRepetition() {
		Command cmd = mock(Command.class);
		when(cmd.repetition()).thenReturn(null);
		CommandBasedMode normalMode = (CommandBasedMode) mode;
		normalMode.executeCommand(cmd);
		verify(registerManager, never()).setLastEdit(null);
		verify(registerManager, never()).setLastEdit(any(Command.class));
	}

}