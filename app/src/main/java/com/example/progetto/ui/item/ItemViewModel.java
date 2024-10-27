package com.example.progetto.ui.item;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ItemViewModel extends ViewModel {
    private final MutableLiveData<List<Item>> items;

    public ItemViewModel() {
        items = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void addItem(Item item) {
        List<Item> currentItems = items.getValue();
        if (currentItems != null) {
            currentItems.add(item);
            items.setValue(currentItems);
        }
    }
}