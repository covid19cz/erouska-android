package arch.view

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import arch.viewmodel.BaseDialogViewModel
import cz.stepansonsky.mvvm.R
import kotlin.reflect.KClass


abstract class BaseDialogFragment<B : ViewDataBinding, VM : BaseDialogViewModel>(@LayoutRes layoutId: Int, viewModelClass: KClass<VM>) :
        BaseArchDialogFragment<B, VM>(layoutId, viewModelClass) {

    companion object {
        const val CANCELED = Activity.RESULT_CANCELED
        const val BUTTON_POSITIVE = AlertDialog.BUTTON_POSITIVE
        const val BUTTON_NEGATIVE = AlertDialog.BUTTON_NEGATIVE
        const val BUTTON_NEUTRAL = AlertDialog.BUTTON_NEUTRAL

        const val ARG_TITLE_RES = "ARG_TITLE_RES"
        const val ARG_MESSAGE_RES = "ARG_MESSAGE_RES"
        const val ARG_POSITIVE_BUTTON_RES = "ARG_POSITIVE_BUTTON_RES"
        const val ARG_NEGATIVE_BUTTON_RES = "ARG_NEGATIVE_BUTTON_RES"
        const val ARG_NEUTRAL_BUTTON_RES = "ARG_NEUTRAL_BUTTON_RES"

        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_MESSAGE = "ARG_MESSAGE"
        const val ARG_POSITIVE_BUTTON = "ARG_POSITIVE_BUTTON"
        const val ARG_NEGATIVE_BUTTON = "ARG_NEGATIVE_BUTTON"
        const val ARG_NEUTRAL_BUTTON = "ARG_NEUTRAL_BUTTON"

        const val ARG_REQUEST_CODE = "ARG_REQUEST_CODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments?.containsKey(ARG_TITLE_RES) == true) {
            viewModel.title = getString(arguments?.getInt(ARG_TITLE_RES)!!)
        } else if (arguments?.containsKey(ARG_TITLE) == true) {
            viewModel.title = arguments?.getString(ARG_TITLE)
        }

        if (arguments?.containsKey(ARG_MESSAGE_RES) == true) {
            viewModel.message = getString(arguments?.getInt(ARG_MESSAGE_RES)!!)
        } else if (arguments?.containsKey(ARG_MESSAGE) == true) {
            viewModel.message = arguments?.getString(ARG_MESSAGE)
        }

        if (arguments?.containsKey(ARG_POSITIVE_BUTTON_RES) == true) {
            viewModel.positiveButton = getString(arguments?.getInt(ARG_POSITIVE_BUTTON_RES)!!)
        } else if (arguments?.containsKey(ARG_POSITIVE_BUTTON) == true) {
            viewModel.positiveButton = arguments?.getString(ARG_POSITIVE_BUTTON)
        }

        if (arguments?.containsKey(ARG_NEGATIVE_BUTTON_RES) == true) {
            viewModel.negativeButton = getString(arguments?.getInt(ARG_NEGATIVE_BUTTON_RES)!!)
        } else if (arguments?.containsKey(ARG_NEGATIVE_BUTTON) == true) {
            viewModel.negativeButton = arguments?.getString(ARG_NEGATIVE_BUTTON)
        }

        if (arguments?.containsKey(ARG_NEUTRAL_BUTTON_RES) == true) {
            viewModel.negativeButton = getString(arguments?.getInt(ARG_NEUTRAL_BUTTON_RES)!!)
        } else if (arguments?.containsKey(ARG_NEUTRAL_BUTTON) == true) {
            viewModel.negativeButton = arguments?.getString(ARG_NEUTRAL_BUTTON)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.root.findViewById<Button>(R.id.button_positive)?.setOnClickListener { dispatchResult(BUTTON_POSITIVE) }
        binding.root.findViewById<Button>(R.id.button_negative)?.setOnClickListener { dispatchResult(BUTTON_NEGATIVE) }
        binding.root.findViewById<Button>(R.id.button_neutral)?.setOnClickListener { dispatchResult(BUTTON_NEUTRAL) }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    protected fun dispatchResult(result: Int) {
        if (targetFragment != null) {
            targetFragment?.onActivityResult(arguments!!.getInt(ARG_REQUEST_CODE), result, null)
        } else {
            (activity as BaseArchActivity<*, *>).onActivityResult(arguments!!.getInt(ARG_REQUEST_CODE), result, null)
        }
        dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        dispatchResult(CANCELED)
    }

    open class Builder {
        private var title: String? = null
        private var titleRes: Int? = null
        private var message: String? = null
        private var messageRes: Int? = null
        private var positiveButton: String? = null
        private var positiveButtonRes: Int? = null
        private var negativeButton: String? = null
        private var negativeButtonRes: Int? = null
        private var neutralButton: String? = null
        private var neutralButtonRes: Int? = null

        fun show(targetFragment: Fragment?, requestCode: Int) {
            val fragment = buildDialog(requestCode)
            fragment.setTargetFragment(targetFragment, requestCode)
            fragment.show(targetFragment?.fragmentManager!!, "dialog_$requestCode")
        }

        fun show(activity: AppCompatActivity?, requestCode: Int) {
            buildDialog(requestCode).show(activity?.supportFragmentManager!!, "dialog_$requestCode")
        }

        protected open fun createArgs(requestCode: Int): Bundle {
            val args = Bundle()

            if (titleRes != null) {
                args.putInt(ARG_TITLE_RES, titleRes!!)
            } else if (title != null) {
                args.putString(ARG_TITLE, title)
            }

            if (messageRes != null) {
                args.putInt(ARG_MESSAGE_RES, messageRes!!)
            } else if (message != null) {
                args.putString(ARG_MESSAGE, message)
            }

            if (positiveButtonRes != null) {
                args.putInt(ARG_POSITIVE_BUTTON_RES, positiveButtonRes!!)
            } else if (positiveButton != null) {
                args.putString(ARG_POSITIVE_BUTTON, positiveButton)
            }

            if (negativeButtonRes != null) {
                args.putInt(ARG_NEGATIVE_BUTTON_RES, negativeButtonRes!!)
            } else if (negativeButton != null) {
                args.putString(ARG_NEGATIVE_BUTTON, negativeButton)
            }

            if (neutralButtonRes != null) {
                args.putInt(ARG_NEUTRAL_BUTTON_RES, neutralButtonRes!!)
            } else if (neutralButton != null) {
                args.putString(ARG_NEUTRAL_BUTTON, neutralButton)
            }

            args.putInt(ARG_REQUEST_CODE, requestCode)
            return args
        }

        open fun newInstance(): BaseDialogFragment<*, *> {
            throw NotImplementedError()
        }

        private fun buildDialog(requestCode: Int): BaseDialogFragment<*, *> {
            val dialog = newInstance()
            dialog.arguments = createArgs(requestCode)
            return dialog
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes title: Int): Builder {
            this.titleRes = title
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.messageRes = message
            return this
        }

        fun setPositiveButton(positiveButton: String): Builder {
            this.positiveButton = positiveButton
            return this
        }

        fun setPositiveButton(@StringRes positiveButton: Int): Builder {
            this.positiveButtonRes = positiveButton
            return this
        }

        fun setNegativeButton(negativeButton: String): Builder {
            this.negativeButton = negativeButton
            return this
        }

        fun setNegativeButton(@StringRes negativeButton: Int): Builder {
            this.negativeButtonRes = negativeButton
            return this
        }

        fun setNeutralButton(negativeButton: String): Builder {
            this.neutralButton = negativeButton
            return this
        }

        fun setNeutralButton(@StringRes negativeButton: Int): Builder {
            this.neutralButtonRes = negativeButton
            return this
        }
    }


}