package com.example.unparche.interfaces.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.unparche.R;
import com.example.unparche.entidades.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrarActivity extends AppCompatActivity {

    EditText mUsername, mPassword;
    Button mRegistrar;

    FirebaseAuth firebaseAuth;
    AwesomeValidation awesomeValidation;
    Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        firebaseAuth = FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(this, R.id.etUsername, Patterns.EMAIL_ADDRESS, R.string.invalid_username);
        awesomeValidation.addValidation(this, R.id.etPassword, ".{6,}",R.string.invalid_password);

        mUsername = findViewById(R.id.etUsername);
        mPassword = findViewById(R.id.etPassword);
        mRegistrar = findViewById(R.id.btnRegistrar);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mUsername.setText(bundle.getString("mail"));
        }

        mRegistrar.setEnabled(true);

        mRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = mUsername.getText().toString();
                String pass = mPassword.getText().toString();

                if(awesomeValidation.validate()){
                    firebaseAuth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(RegistrarActivity.this, "Correo de confirmaci??n enviado", Toast.LENGTH_SHORT).show();
                                            instUsuario(user.getUid(), user.getEmail());
                                            term();
                                        }else{
                                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                            dameToastDeError(errorCode);
                                        }
                                    }
                                });
                            }else{
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                dameToastDeError(errorCode);
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegistrarActivity.this, R.string.completar_campos, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void term() {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },1000);

    }

    private void instUsuario(String uid, String email) {
        //String mailEd = mail.replace(".",MainActivity.DOT_REPLACEMENT);
        usuario = new Usuario(email);
        FirebaseDatabase.getInstance().getReference(MainActivity.PATH_USUARIOS).child(uid).setValue(usuario);
    }

    private void dameToastDeError(String error) {

        switch (error) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(RegistrarActivity.this, "El formato del token personalizado es incorrecto. Por favor revise la documentaci??n", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(RegistrarActivity.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(RegistrarActivity.this, "La credencial de autenticaci??n proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(RegistrarActivity.this, "La direcci??n de correo electr??nico est?? mal formateada.", Toast.LENGTH_LONG).show();
                mUsername.setError("La direcci??n de correo electr??nico est?? mal formateada.");
                mUsername.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(RegistrarActivity.this, "La contrase??a no es v??lida o el usuario no tiene contrase??a.", Toast.LENGTH_LONG).show();
                mPassword.setError("la contrase??a es incorrecta ");
                mPassword.requestFocus();
                mPassword.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(RegistrarActivity.this, "Las credenciales proporcionadas no corresponden al usuario que inici?? sesi??n anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(RegistrarActivity.this,"Esta operaci??n es sensible y requiere autenticaci??n reciente. Inicie sesi??n nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(RegistrarActivity.this, "Ya existe una cuenta con la misma direcci??n de correo electr??nico pero diferentes credenciales de inicio de sesi??n. Inicie sesi??n con un proveedor asociado a esta direcci??n de correo electr??nico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(RegistrarActivity.this, "La direcci??n de correo electr??nico ya est?? siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                mUsername.setError("La direcci??n de correo electr??nico ya est?? siendo utilizada por otra cuenta.");
                mUsername.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(RegistrarActivity.this, "Esta credencial ya est?? asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(RegistrarActivity.this, "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(RegistrarActivity.this, "La credencial del usuario ya no es v??lida. El usuario debe iniciar sesi??n nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(RegistrarActivity.this, "No hay ning??n registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(RegistrarActivity.this, "La credencial del usuario ya no es v??lida. El usuario debe iniciar sesi??n nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(RegistrarActivity.this, "Esta operaci??n no est?? permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(RegistrarActivity.this, "La contrase??a proporcionada no es v??lida..", Toast.LENGTH_LONG).show();
                mPassword.setError("La contrase??a no es v??lida, debe tener al menos 6 caracteres");
                mPassword.requestFocus();
                break;

        }

    }
}