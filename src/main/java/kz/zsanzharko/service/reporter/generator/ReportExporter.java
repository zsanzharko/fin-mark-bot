package kz.zsanzharko.service.reporter.generator;

import kz.zsanzharko.model.Report;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public interface ReportExporter {
    InputFile export(Report report);
}
