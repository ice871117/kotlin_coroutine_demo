package com.tencent.kotlincoroutine

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity: AppCompatActivity() {

    companion object {
        val TEST_CASE = listOf(CommonUsage,
                CompoundUsage,
                ChannelUsage,
                DispatcherUsage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val spinner = findViewById<Spinner>(R.id.spinner1)
        spinner.adapter = MySpinnerAdapter()

        val logText = findViewById<TextView>(R.id.log_text)

        findViewById<Button>(R.id.go).setOnClickListener {
            (spinner.selectedItem as? ITestCase)?.test()
        }

        findViewById<Button>(R.id.clear).setOnClickListener {
            logText.text = ""
        }

        val weakTextView = WeakReference(logText)
        COLLECTOR = { log ->
            GlobalScope.launch(Main) {
                weakTextView.get()?.apply {
                    append("$log\n")
                }
            }

        }

    }

    inner class MySpinnerAdapter: BaseAdapter() {

        override fun isEmpty() = false

        override fun getCount() = TEST_CASE.size

        override fun getItem(position: Int): Any {
            return TEST_CASE[position]
        }

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = buildText(getItem(position).javaClass.simpleName)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?) = buildText(getItem(position).javaClass.simpleName)

        private fun buildText(name: String): TextView {
            val text = TextView(this@MainActivity)
            text.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)
            text.text = name
            text.setPadding(20, 0, 0, 0)
            text.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            return text
        }

    }
}