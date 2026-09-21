package com.delprod.kanban.UI.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.delprod.kanban.MainActivity
import com.delprod.kanban.core.App
import com.delprod.kanban.databinding.ActivityLoginBinding
import com.delprod.kanban.utils.launchAndCollectIn

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel = LoginViewModel((application as App).authRepository)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.username.text.toString().trim(),
                binding.password.text.toString()
            )
        }

        viewModel.state.launchAndCollectIn(this) { state ->
            binding.progress.visibility = if (state is LoginUiState.Loading) android.view.View.VISIBLE else android.view.View.GONE
            binding.loginButton.isEnabled = state !is LoginUiState.Loading
            when (state) {
                is LoginUiState.Error -> binding.errorMessage.text = state.message
                is LoginUiState.Success -> {
                    binding.errorMessage.text = ""
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
                else -> binding.errorMessage.text = ""
            }
        }
    }
}
