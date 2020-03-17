package arch.adapter

interface RecyclerLayoutStrategy {
    fun getLayoutId(item: Any): Int
}