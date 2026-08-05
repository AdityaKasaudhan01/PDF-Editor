package com.pdfwordeditor.app.documentmodel;

import java.util.List;

public record PageModel(int pageNumber, double width, double height, List<TextRunModel> textRuns) {}
