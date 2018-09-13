package edu.uga.eits.android


import android.os.Bundle
import android.util.Log
import edu.uga.eits.android.extensions.Keys
import edu.uga.eits.android.extensions.getArg
import edu.uga.eits.android.extensions.getArgsMap
import edu.uga.eits.android.ui.RxListFragment
import edu.uga.eits.android.viewmodel.ListViewModels
import com.google.android.gms.analytics.HitBuilders



class ListViewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        Log.d("--ListViewActivity12--","~~~intent.getArgsMap()~~~ => ${intent.getArgsMap()}" )
        val viewModel = intent.getArg(Keys.LIST_ID).let{ListViewModels.of(it)}
        viewModel.bindTitle(this)
        setFragment(RxListFragment.newInstance(intent.getArgsMap()),R.id.activity_base_content)
    }



}
