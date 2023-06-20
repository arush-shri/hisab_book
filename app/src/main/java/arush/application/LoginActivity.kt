package arush.application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import arush.application.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private var verificationId : String? = null
    private var edtPhone: String? = null
    private  var edtOTP: EditText? = null
    lateinit var loginBinding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        setContentView(view)
        loginBinding.getOTP.setOnClickListener {
            edtPhone = loginBinding.phoneNum.text.toString()
            if(edtPhone.isNullOrEmpty()){
                Toast.makeText(this@LoginActivity, "Please Enter you Number", Toast.LENGTH_SHORT).show()
            }
            else
            {
                sendVerificationCode("+91$edtPhone")
                loginBinding.textInputLayout2.isVisible = true
                loginBinding.Verifybutton.isVisible = true
                loginBinding.getOTP.isVisible = false
                loginBinding.textInputLayout.isEnabled = false
                loginBinding.textInputLayout.isFocusable = false
            }
        }
        loginBinding.Verifybutton.setOnClickListener {
            val otp = loginBinding.OTPedit.text.toString()
            if(otp.isNullOrEmpty())
            {
                Toast.makeText(applicationContext,"Please provide OTP", Toast.LENGTH_SHORT).show()
            }
            else
            {
                verifyCode(otp)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential)
    {
        auth.signInWithCredential(credential).
                addOnCompleteListener { task->
                    if(task.isSuccessful)
                    {
                        Toast.makeText(this@LoginActivity,"Welcome to Hisab Book",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("user_id", edtPhone)
                        startActivity(intent)
                        finish()

                    }
                    else
                    {
                        Toast.makeText(applicationContext, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
    }
    private fun sendVerificationCode(phoneNum: String)
    {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId = p0
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                val code = p0.smsCode
                if(code != null)
                {
                    edtOTP?.setText(code)
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(applicationContext, p0.message, Toast.LENGTH_LONG).show()
            }
        }
    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithCredential(credential)
    }
}