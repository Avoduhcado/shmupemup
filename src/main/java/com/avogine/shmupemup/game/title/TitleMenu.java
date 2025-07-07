package com.avogine.shmupemup.game.title;

import static org.lwjgl.nuklear.Nuklear.*;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import com.avogine.game.ui.NuklearGUI;
import com.avogine.game.ui.nuklear.NuklearUIElement;
import com.avogine.io.Window;
import com.avogine.shmupemup.game.Title.TitleScene;

/**
 *
 */
public class TitleMenu extends NuklearUIElement {

	private final TitleScene scene;
	
	private String windowTitle;
	private NkRect position;
	private int windowOpts;
	
//	private AudioConfigUI audioConfig;
	private String audioWindowTitle;
	private NkRect audioPosition;
	private int audioWindowOpts;
	
	/**
	 * @param nuklearContext
	 * @param scene
	 */
	public TitleMenu(NuklearGUI nuklearContext, TitleScene scene) {
		super(nuklearContext, scene.getProjection().getWidth(), scene.getProjection().getHeight());
		this.scene = scene;
	}
	
	@Override
	protected void init(NkContext context) {
		windowOpts = NK_WINDOW_NO_SCROLLBAR;
		windowTitle = "TITLE_MENU";
		
//		audioConfig = new AudioConfigUI();
		audioWindowTitle = "AUDIO_SETTINGS";
		audioWindowOpts = NK_WINDOW_BORDER;
		
		position = NkRect.malloc();
		nk_begin(context, windowTitle, position, 0);
		nk_end(context);
		nk_window_show(context, windowTitle, NK_SHOWN);

		audioPosition = NkRect.malloc();
		nk_begin(context, audioWindowTitle, audioPosition, 0);
		nk_end(context);
		nk_window_show(context, audioWindowTitle, NK_HIDDEN);

		nk_window_set_focus(context, windowTitle);
	}

	@Override
	public void layout(NkContext context, Window window) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			// TODO Move this Style stuff to some GameUIStyle class or at least into the register method to only perform it once
			NkStyle style = context.style(); 
			NkColor background = NkColor.malloc(stack);
			background.r((byte) 255).g((byte) 0).b((byte) 0).a((byte) 0);
			style.window().fixed_background().data().color().set(background);

			NkColor text = NkColor.malloc(stack);
			text.r((byte) 255).g((byte) 255).b((byte) 255).a((byte) 255);
			style.text().color().set(text);

			NkColor disabledItemColor = NkColor.malloc(stack);
			disabledItemColor.r((byte) 40).g((byte) 40).b((byte) 40).a((byte) 255);
			NkStyleItem disabledStyleItem = nk_style_item_color(disabledItemColor, NkStyleItem.malloc(stack));
			NkColor disabledTextColor = NkColor.malloc(stack);
			disabledTextColor.r((byte) 60).g((byte) 60).b((byte) 60).a((byte) 255);
			NkStyleButton disabledStyleButton = NkStyleButton.malloc(stack).set(style.button());
			disabledStyleButton.normal(disabledStyleItem);
			disabledStyleButton.active(disabledStyleItem);
			disabledStyleButton.hover(disabledStyleItem);
			disabledStyleButton.border_color(disabledTextColor);
			disabledStyleButton.text_background(disabledTextColor);
			disabledStyleButton.text_normal(disabledTextColor);
			disabledStyleButton.text_active(disabledTextColor);
			disabledStyleButton.text_hover(disabledTextColor);

			nk_window_show_if(context, windowTitle, NK_HIDDEN, nk_window_is_hidden(context, windowTitle));
			nk_window_set_focus(context, windowTitle);
			nk_window_show_if(context, audioWindowTitle, NK_HIDDEN, nk_window_is_hidden(context, audioWindowTitle));
			nk_window_set_focus(context, audioWindowTitle);
			
			position.x((displayWidth * 0.5f) - 100).y(displayHeight * 0.5f).w(200).h(200);
			if (nk_begin(context, windowTitle, position, windowOpts)) {
				nk_layout_row_dynamic(context, 35, 1);
				if (nk_button_label(context, "New Game")) {
					scene.setStartGame(true);
				}

				if (nk_button_label_styled(context, scene.saveGamesExist() ? style.button() : disabledStyleButton, "Load Game") && scene.saveGamesExist()) {
					// TODO No saved games
				}

				if (nk_button_label(context, "Options")) {
					showOptions(context);
				}

				if (nk_button_label(context, "Quit Game")) {
					GLFW.glfwSetWindowShouldClose(window.getId(), true);
				}
				// Interesting stuff. Probably better off used for a complete menu that's aware of the rest of the UI context so you can control tabbing between different windows.
				// TODO Look more into keyboard navigable menus
				//				if (nk_window_has_focus(context)) {
				//					if (nk_input_is_key_released(context.input(), NK_KEY_ENTER)) {
				//						loadNewGame();
				//					}
				//				} else if (nk_input_is_key_released(context.input(), NK_KEY_TAB)) {
				//					nk_window_set_focus(context, "TITLE_MENU");
				//				}
			}
			nk_end(context);
			
			background.r((byte) 64).g((byte) 64).b((byte) 64).a((byte) 255);
			style.window().fixed_background().data().color().set(background);

			text.r((byte) 255).g((byte) 255).b((byte) 255).a((byte) 255);
			style.text().color().set(text);
			
			audioPosition.x((displayWidth * 0.5f) - 200).y(displayHeight * 0.3f).w(400).h(230);
			if (nk_begin(context, audioWindowTitle, audioPosition, audioWindowOpts)) {
//				audioConfig.layout(context);

				nk_spacer(context);

				nk_layout_row_begin(context, NK_DYNAMIC, 25, 3);
				nk_layout_row_push(context, 0.33f);
				nk_spacer(context);
				if (nk_button_label(context, "Close")) {
					hideOptions(context);
				}
				nk_layout_row_end(context);
			}
			nk_end(context);
		}
	}
	
	@Override
	public void cleanup() {
		Objects.requireNonNull(position).free();
		Objects.requireNonNull(audioPosition).free();
	}
	
	/**
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		this.displayWidth = width;
		this.displayHeight = height;
	}
	
	private void showOptions(NkContext context) {
		nk_window_show(context, audioWindowTitle, NK_SHOWN);
		nk_window_show(context, windowTitle, NK_HIDDEN);
		nk_window_set_focus(context, audioWindowTitle);
	}
	
	private void hideOptions(NkContext context) {
		nk_window_show(context, audioWindowTitle, NK_HIDDEN);
		nk_window_show(context, windowTitle, NK_SHOWN);
		nk_window_set_focus(context, windowTitle);
	}

}
