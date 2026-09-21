package com.delprod.kanban

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.delprod.kanban.UI.auth.LoginActivity
import com.delprod.kanban.UI.importui.ImportActivity
import com.delprod.kanban.UI.clients.ClientsActivity
import com.delprod.kanban.UI.goods.GoodsActivity
import com.delprod.kanban.UI.label.LabelListActivity
import com.delprod.kanban.UI.label.LabelPrintActivity
import com.delprod.kanban.UI.orders.OrderActivity
import com.delprod.kanban.UI.placing.PlacingOrderActivity
import com.delprod.kanban.UI.settings.PasswordDialog
import com.delprod.kanban.UI.settings.SettingsActivity
import com.delprod.kanban.core.App
import com.delprod.kanban.data.Settings
import com.delprod.kanban.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!(application as App).authRepository.isLoggedIn()) {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        settings = (this.application as App).settings
        binding.ordersButton.setOnClickListener {
            val intent = Intent(this@MainActivity, OrderActivity::class.java)
            startActivity(intent)
        }

        binding.settingsButton.setOnClickListener {
            if (settings.password() != null){
                PasswordDialog.create(this, layoutInflater, settings){
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }else {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        binding.goodsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, GoodsActivity::class.java)
            startActivity(intent)
        }

        binding.importButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ImportActivity::class.java)
            startActivity(intent)
        }

        binding.labelBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, LabelListActivity::class.java)
            startActivity(intent)
        }

        binding.labelBtn.setOnLongClickListener {
            val intent = Intent(this@MainActivity, LabelPrintActivity::class.java)
            startActivity(intent)
            return@setOnLongClickListener true
        }

        binding.clientsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ClientsActivity::class.java)
            startActivity(intent)
        }

        binding.placingOrderButton.setOnClickListener {
            val intent = Intent(this@MainActivity, PlacingOrderActivity::class.java)
            startActivity(intent)
        }
    }
}