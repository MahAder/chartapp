package com.tarashor.chartlib;

interface IValueConverter<T> {
    String format(T v);
    float valueToPixels(T v);
    T pixelsToValue(float pixels);
}
