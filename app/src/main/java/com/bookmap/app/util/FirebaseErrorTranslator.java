package com.bookmap.app.util;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseErrorTranslator {
    public static String translate(Exception exception) {
        if (exception == null) {
            return "Erro desconhecido.";
        }

        if (exception instanceof FirebaseNetworkException) {
            return "Erro de conexão. Verifique sua internet.";
        }

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    return "O formato do e-mail inserido é inválido.";
                case "ERROR_WRONG_PASSWORD":
                case "ERROR_USER_NOT_FOUND":
                    return "E-mail ou senha incorretos.";
                case "ERROR_USER_DISABLED":
                    return "Esta conta foi desativada.";
                case "ERROR_TOO_MANY_REQUESTS":
                    return "Muitas tentativas de login bloqueadas temporariamente. Tente novamente mais tarde.";
                case "ERROR_WEAK_PASSWORD":
                    return "A senha digitada é muito fraca (mínimo de 6 caracteres).";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Este e-mail já está sendo utilizado por outra conta.";
                case "ERROR_USER_TOKEN_EXPIRED":
                    return "A sessão expirou. Faça login novamente.";
                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "Falha de rede. Verifique sua conexão com a internet.";
                default:
                    break;
            }
        }

        String msg = exception.getMessage();
        if (msg != null) {
            String lowerMsg = msg.toLowerCase();
            if (lowerMsg.contains("badly formatted") || lowerMsg.contains("invalid email")) {
                return "O formato do e-mail é inválido.";
            } else if (lowerMsg.contains("already in use") || lowerMsg.contains("already exists")) {
                return "Este e-mail já está cadastrado.";
            } else if (lowerMsg.contains("no user record") || lowerMsg.contains("user not found")) {
                return "E-mail ou senha incorretos.";
            } else if (lowerMsg.contains("password must be") || lowerMsg.contains("weak password")) {
                return "A senha deve ter pelo menos 6 caracteres.";
            } else if (lowerMsg.contains("wrong password") || lowerMsg.contains("invalid password")) {
                return "E-mail ou senha incorretos.";
            } else if (lowerMsg.contains("network error") || lowerMsg.contains("timeout")) {
                return "Falha na rede. Verifique sua conexão com a internet.";
            } else if (lowerMsg.contains("too many requests")) {
                return "Muitas tentativas bloqueadas temporariamente. Tente novamente mais tarde.";
            }
        }

        return "Erro: " + exception.getLocalizedMessage();
    }
}
