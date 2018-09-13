package edu.uga.eits.android.ui


import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import edu.uga.eits.android.R
import edu.uga.eits.android.extensions.inflate
import edu.uga.eits.android.module.Navigator
import kotlinx.android.synthetic.main.grid_item.view.*


enum class HomeModule{
    HOME,Parking;
    fun getTitle() =  this.toString().replace("_"," ")
    fun getIcon() = when(this){
        HOME -> R.drawable.ic_home
        Parking -> R.drawable.ic_park
        

    }
    fun getId() =     this.toString().replace("_","")

    fun preferredFontSize() = null
    companion object {

        val moduleForMenu = mapOf((R.id.navigation_home to HomeModule.HOME))
        val naviagtionMenu = moduleForMenu.entries.associateBy({ it.value },{it.key})


    }
}
class HomeGridAdaper(val mContext:Context): RecyclerView.Adapter<HomeGridAdaper.ViewHolder>(){
   // private val excludeFromHome = listOf(HomeModule.HOME)
    private  var mItems =  HomeModule.values()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =  holder.bind(mItems[position],mContext)
    override fun getItemCount()= mItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.grid_item))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(item: HomeModule,context: Context) =
            with(itemView) {
                textGrid.text = item.getTitle()
                item.preferredFontSize()?.let{textGrid.textSize = it}
                imageGrid.setImageResource(item.getIcon())
                setOnClickListener { Navigator.selectedModule.onNext(Pair(item,context)) }
            }
        }
}






