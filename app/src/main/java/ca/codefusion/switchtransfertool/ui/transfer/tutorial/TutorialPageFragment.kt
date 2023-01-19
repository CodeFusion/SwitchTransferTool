package ca.codefusion.switchtransfertool.ui.transfer.tutorial

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ca.codefusion.switchtransfertool.databinding.TutorialPageFragmentBinding

class TutorialPageFragment(
    private val image: Drawable?,
    private val bodyText: String,
    private val actionButtonText: String? = null,
    private val onActionButtonClickEvent: View.OnClickListener? = null,
    private val imageDesc: String? = null
): Fragment() {

    private lateinit var binding: TutorialPageFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TutorialPageFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (getView() == null) {
            return
        }

        val ivImage = binding.slideImage
        val tvBody = binding.slideBody
        val btnAction = binding.slideActionButton

        ivImage.setImageDrawable(image)
        ivImage.contentDescription = imageDesc
        tvBody.text = bodyText

        if (actionButtonText != null) {
            btnAction.visibility = View.VISIBLE
            btnAction.text = actionButtonText
            btnAction.setOnClickListener(onActionButtonClickEvent)
        }

    }
}