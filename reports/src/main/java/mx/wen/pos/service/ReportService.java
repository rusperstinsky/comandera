package mx.wen.pos.service;

import java.text.ParseException;
import java.util.Date;

public interface ReportService {

    String obtenerTicketVenta( String idOrder ) throws ParseException;

}
