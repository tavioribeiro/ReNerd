package com.example.renerd.view_models


data class FiltersTabsListModel (
    val productsList: MutableList<FiltersTabsListItemModel>,
    val subjectsList: MutableList<FiltersTabsListItemModel>,
    val guestsList: MutableList<FiltersTabsListItemModel>,
    val yearsList: MutableList<FiltersTabsListItemModel>
)


data class FiltersTabsListItemModel (
    val label: String,
    val type: String,
    val status: Boolean
)

