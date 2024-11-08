package com.example.progetto.ui.item;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.data.model.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<List<ItemUtils>> items;

    public ItemViewModel() {
        items = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<ItemUtils>> getItems() {
        return items;
    }

    public void addItem(ItemUtils item) {
        List<ItemUtils> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.add(item);
            items.setValue(currentItems);
        }
    }
}