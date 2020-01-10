package me.yonniton.forex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import me.yonniton.forex.databinding.MainActivityBinding
import me.yonniton.forex.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(save: Bundle?) {
        super.onCreate(save)

        viewModel = ViewModelProviders.of(this)
            .get(MainViewModel::class.java)
        setContentView(R.layout.main_activity)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
            .also { binding ->
                binding.viewModel = viewModel
                lifecycle.addObserver(viewModel)
            }
    }
}
