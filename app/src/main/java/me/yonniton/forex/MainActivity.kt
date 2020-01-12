package me.yonniton.forex

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.lifecycle.ViewModelProviders
import me.yonniton.forex.databinding.MainActivityBinding
import me.yonniton.forex.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmStatic @BindingAdapter("android:text")
        fun setNumericText(view: EditText, numeral: Double) {
            view.setText(numeral.toString())
        }

        @JvmStatic @InverseBindingAdapter(attribute = "android:text")
        fun getNumericText(view: EditText): Double {
            return view.text.toString().toDouble()
        }
    }

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
