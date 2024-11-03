package com.example.renerd.view_models


data class FiltersTabsListModel (
    val productsList: MutableList<FiltersTabsItemModel>,
    val subjectsList: MutableList<FiltersTabsItemModel>,
    val guestsList: MutableList<FiltersTabsItemModel>,
    val yearsList: MutableList<FiltersTabsItemModel>
)


data class FiltersTabsItemModel (
    var id : Int ? = null,
    var label: String,
    var type: String,
    var status: Boolean
)

