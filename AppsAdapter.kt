package com.optimizerpro.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.optimizerpro.databinding.ItemAppBinding
import com.optimizerpro.model.AppInfo

class AppsAdapter(private var appList: List<AppInfo>) :
    RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    /**
     * El ViewHolder contiene las vistas para cada elemento de la lista.
     */
    class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = appList[position]
        holder.binding.apply {
            appIcon.setImageDrawable(appInfo.icon)
            appName.text = appInfo.appName
            appPackageName.text = appInfo.packageName
        }
    }

    override fun getItemCount(): Int = appList.size

    /**
     * Actualiza la lista de aplicaciones que muestra el adaptador.
     *
     * @param newList La nueva lista de aplicaciones a mostrar.
     */
    fun updateData(newList: List<AppInfo>) {
        this.appList = newList
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado.
    }
}