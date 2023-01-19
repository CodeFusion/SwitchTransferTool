package ca.codefusion.switchtransfertool.ui.transfer.tutorial

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import ca.codefusion.switchtransfertool.R

class TutTransformation: ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        val increment = 1.8f

        val ivImage = view.findViewById<ImageView>(R.id.slideImage)
        val tvBody = view.findViewById<TextView>(R.id.slideBody)

        ivImage.alpha = 0f

        ivImage.animate().alpha(1f).duration = 0
    }
}