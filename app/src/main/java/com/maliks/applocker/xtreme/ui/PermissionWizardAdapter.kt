package com.maliks.applocker.xtreme.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maliks.applocker.xtreme.R

class PermissionWizardAdapter(private val context: Context, private val steps: List<Int>) : RecyclerView.Adapter<PermissionWizardAdapter.WizardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WizardViewHolder {
        val view = LayoutInflater.from(context).inflate(viewType, parent, false)
        return WizardViewHolder(view)
    }

    override fun onBindViewHolder(holder: WizardViewHolder, position: Int) {

        val layoutId = steps[position]

        if (layoutId == R.layout.wizard_step_three) {
            val gifImageView = holder.itemView.findViewById<ImageView>(R.id.usageAccessGifImageView)
            Glide.with(context)
                .asGif()
                .load(R.drawable.access) // Replace with your GIF resource name
                .into(gifImageView)
        }
        if(layoutId == R.layout.wizard_step_four) {
            val gifImageView = holder.itemView.findViewById<ImageView>(R.id.overlayGifImageView)
            Glide.with(context)
                .asGif()
                .load(R.drawable.overlay) // Replace with your GIF resource name
                .into(gifImageView)
        }


    }

    override fun getItemCount(): Int = steps.size

    override fun getItemViewType(position: Int): Int {
        return steps[position]
    }

    class WizardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
