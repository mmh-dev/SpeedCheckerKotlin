package com.mmh.speedcheckerkotlin.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mmh.speedcheckerkotlin.R
import com.mmh.speedcheckerkotlin.databinding.ActivityMainBinding
import com.mmh.speedcheckerkotlin.viewmodels.SpeedViewModel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel: SpeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.testBtn.setOnClickListener {
            viewModel.getSpeeds()
            binding.testBtn.apply {
                text = getString(R.string.testing)
                isEnabled = false
                setBackgroundColor(resources.getColor(androidx.appcompat.R.color.material_grey_300))
            }
            binding.resultTitle.text = getString(R.string.result_title)
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                launch {
                    viewModel.currentSpeed.collect {
                        binding.tvResult.text = it
                    }
                }
                launch {
                    viewModel.isJobCancelled.collect { isJobCancelled ->
                        if (isJobCancelled) {
                            binding.testBtn.apply {
                                text = getString(R.string.start)
                                isEnabled = true
                                setBackgroundColor(resources.getColor(R.color.purple_500))
                            }
                            binding.resultTitle.text = getString(R.string.result_title_final)
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}