package com.aaron.proyectoteo.ventanas;

import java.time.LocalDate;
import org.jdatepicker.DateModel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

final class DatePickerUtils {

    private DatePickerUtils() {
    }

    static JDatePicker crear(LocalDate fechaInicial) {
        UtilDateModel modelo = new UtilDateModel();
        if (fechaInicial != null) {
            modelo.setDate(
                    fechaInicial.getYear(),
                    fechaInicial.getMonthValue() - 1,
                    fechaInicial.getDayOfMonth());
            modelo.setSelected(true);
        }

        JDatePicker selector = new JDatePicker(modelo);
        selector.setTextEditable(false);
        selector.setButtonFocusable(false);
        return selector;
    }

    static LocalDate obtener(JDatePicker selector) {
        DateModel<?> modelo = selector.getModel();
        if (!modelo.isSelected() || modelo.getValue() == null) {
            return null;
        }
        return LocalDate.of(modelo.getYear(), modelo.getMonth() + 1, modelo.getDay());
    }
}
