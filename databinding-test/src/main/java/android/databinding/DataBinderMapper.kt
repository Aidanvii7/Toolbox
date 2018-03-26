package android.databinding

/**
 * Data binding generates a class like this, where [ViewDataBinding] accesses the static constant [TARGET_MIN_SDK].
 * This version is needed to create or mock a [ViewDataBinding] class as the test sources cant see the generated one.
 */
object DataBinderMapper {
    const val TARGET_MIN_SDK = 19
}