package com.bookmap.app.functional;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bookmap.app.LoginActivity;
import com.bookmap.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;

/**
 * Testes funcionais para LoginActivity.
 * Verifica o fluxo de login, navegacao e validacao de campos.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class LoginActivityTest {

    @Test
    public void testActivityCreation() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        assertNotNull(activity);
    }

    @Test
    public void testLoginButtonExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        Button btnLogin = activity.findViewById(R.id.btnLogin);
        assertNotNull("Botao de login deve existir", btnLogin);
        assertEquals("ACESSAR SISTEMA", btnLogin.getText().toString());
    }

    @Test
    public void testEmailFieldExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        EditText editEmail = activity.findViewById(R.id.editEmail);
        assertNotNull("Campo de email deve existir", editEmail);
    }

    @Test
    public void testPasswordFieldExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        EditText editPassword = activity.findViewById(R.id.editPassword);
        assertNotNull("Campo de senha deve existir", editPassword);
    }

    @Test
    public void testRegisterLinkExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        TextView tvRegister = activity.findViewById(R.id.tvRegister);
        assertNotNull("Link de cadastro deve existir", tvRegister);
    }

    @Test
    public void testForgotPasswordLinkExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        TextView tvForgotPassword = activity.findViewById(R.id.tvForgotPassword);
        assertNotNull("Link de recuperar senha deve existir", tvForgotPassword);
    }

    @Test
    public void testGuestLinkExists() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        TextView tvGuest = activity.findViewById(R.id.tvGuest);
        assertNotNull("Link de convidado deve existir", tvGuest);
    }

    @Test
    public void testEmptyFieldsShowToast() {
        LoginActivity activity = Robolectric.buildActivity(LoginActivity.class)
                .create().resume().get();
        Button btnLogin = activity.findViewById(R.id.btnLogin);
        btnLogin.performClick();

        String toastText = ShadowToast.getTextOfLatestToast();
        assertNotNull("Toast de erro deve aparecer para campos vazios", toastText);
    }
}
