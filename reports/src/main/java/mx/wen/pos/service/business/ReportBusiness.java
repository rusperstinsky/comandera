package mx.wen.pos.service.business;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.OrderSpecifier;
import mx.wen.pos.model.*;
import mx.wen.pos.repository.*;
import mx.wen.pos.service.impl.ReportServiceImpl;
import net.sf.jasperreports.engine.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;
import java.util.List;

@Component
public class ReportBusiness {

    private static Logger log = LoggerFactory.getLogger( ReportServiceImpl.class );

    @Resource
    private SucursalRepository sucursalRepository;

    private static final String TAG_CANCELADO = "T";

    public String CompilayGeneraReporte( org.springframework.core.io.Resource template, Map<String, Object> parametros, File report ) {

        try {

            JasperReport jasperReport = JasperCompileManager.compileReport( template.getInputStream() );
            JasperPrint jasperPrint = JasperFillManager.fillReport( jasperReport, parametros, new JREmptyDataSource() );
            JasperExportManager.exportReportToPdfFile( jasperPrint, report.getPath() );
            Desktop.getDesktop().open( report );
            log.info( "Mostrar Reporte" );

            return report.getPath();
        } catch ( JRException e ) {
            log.error( "error al compilar y generar reporte", e );
        } catch ( IOException e ) {
            log.error( "error al compilar y generar reporte", e );
        }
        return report.getPath();
    }

}
