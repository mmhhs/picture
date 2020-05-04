package com.little.picture.listener;

import com.little.picture.model.ImageEntity;

import java.util.List;

public interface IOnCheckListener{
    void onCheck(List<ImageEntity> chooseList);
}