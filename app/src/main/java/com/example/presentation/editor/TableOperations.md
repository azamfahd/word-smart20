Looking at TableBlock:
data class TableCellModel(
    val textBlocks: List<TextBlock>,
    val backgroundColor: Color = Color.Transparent,
    val isRtl: Boolean = false
)

data class TableBlock(
    override val id: String,
    val rows: Int,
    val cols: Int,
    val cells: Map<String, TableCellModel>,
    val isRtl: Boolean = false
) : DocumentBlock()

To add a row:
We need the active table ID and active cell ID.
RibbonEvent.OnAddTableRowClicked(val after: Boolean)
RibbonEvent.OnAddTableColClicked(val after: Boolean)
RibbonEvent.OnDeleteTableRowClicked()
RibbonEvent.OnDeleteTableColClicked()

Let's grep for Table events.
