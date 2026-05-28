package com.bookmap.app.functional;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bookmap.app.ForgotPasswordActivity;
import com.bookmap.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ForgotPasswordActivityTest {

    @Test
    public void testActivityCreation() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        assertNotNull(activity);
    }

    @Test
    public void testEmailFieldExists() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        EditText editEmail = activity.findViewById(R.id.editEmail);
        assertNotNull("Campo de email deve existir", editEmail);
    }

    @Test
    public void testVerifyEmailButtonExists() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        Button btnVerify = activity.findViewById(R.id.btnVerifyEmail);
        assertNotNull("Botao de verificar email deve existir", btnVerify);
    }

    @Test
    public void testPasswordFieldsDisabledInitially() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        EditText editNewPwd = activity.findViewById(R.id.editNewPassword);
        EditText editConfirm = activity.findViewById(R.id.editConfirmPassword);

        assertFalse("Campo de nova senha deve estar desabilitado", editNewPwd.isEnabled());
        assertFalse("Campo de confirmar senha deve estar desabilitado", editConfirm.isEnabled());
    }

    @Test
    public void testResetButtonDisabledInitially() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        Button btnReset = activity.findViewById(R.id.btnResetPassword);
        assertFalse("Botao de redefinir deve estar desabilitado", btnReset.isEnabled());
    }

    @Test
    public void testBackButtonExists() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        TextView btnBack = activity.findViewById(R.id.btnBack);
        assertNotNull("Botao voltar deve existir", btnBack);
    }

    @Test
    public void testEmptyEmailShowsToast() {
        ForgotPasswordActivity activity = Robolectric.buildActivity(ForgotPasswordActivity.class)
                .create().resume().get();
        Button btnVerify = activity.findViewById(R.id.btnVerifyEmail);
        btnVerify.performClick();

        String toast = ShadowToast.getTextOfLatestToast();
        assertNotNull("Toast deve aparecer para email vazio", toast);
        assertTrue(toast.contains("e-mail"));
    }
}
