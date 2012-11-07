package org.dyndns.phpusr.graph;

import com.mxgraph.io.gd.mxGdDocument;
import com.mxgraph.io.mxGdCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.*;
import com.mxgraph.view.mxGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * 
 * @author phpusr
 * Date: 27.05.12
 * Time: 17:57
 */

public class GraphUtil {

    private final mxGraph graph;
    private Object parent;
    private final mxGraphComponent graphComponent;
    private final JFrame frame;
    private int countVertex = 0;
    private final Logger logger;


    public GraphUtil(GraphEditor frame) {
        logger = LoggerFactory.getLogger(GraphUtil.class);
        this.frame = frame;

        graph = new mxGraph();
        customGraph(graph);
        parent = graph.getDefaultParent();

        graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);

        graph.getModel().addListener(mxEvent.CHANGE, new mxEventSource.mxIEventListener() {
            public void invoke(Object sender, mxEventObject evt) {
                logger.debug("CHANGE");
                //onChange();
            }
        });
        graph.addListener(mxEvent.ADD_CELLS, new mxEventSource.mxIEventListener() {
            public void invoke(Object sender, mxEventObject evt) {
                logger.debug("ADD_CELLS");

                //changeEdgeTitles();
                //resetStyleCells((Object[]) evt.getProperty("cells"));

                //graph.refresh();
            }
        });
        getGraphComponent().getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseReleased(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    logger.debug("Delete object");
                    deleteCell();
                }
            }
        });

    }

    /**
     * Включение и отключение опций графа
     * @param graph Граф
     */
    private void customGraph(mxGraph graph) {
        graph.setAllowDanglingEdges(false);
    }

    /**
     * Запускается при изменении графа
     */
    private void onChange() {
        //Сброс стиля для веришн
        resetStyleCells(graph.getChildVertices(parent));
        //Сброс стиля для граней
        resetStyleCells(graph.getChildEdges(parent));

        graph.refresh();
    }

    /**
     * Показывает диаметр на изображении графа
     * @param diametr Диаметр
     */
    private void showDiametr(mxCell diametr) {
        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(Const.STROKECOLOR_DIAMETR), new Object[]{diametr});
    }

    /**
     * Показывает радиус на изображении графа
     * @param radius Радиус
     */
    private void showRadius(mxCell radius) {
        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(Const.STROKECOLOR_RADIUS), new Object[]{radius});
        if (radius.getSource() != null) {
            graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(Const.STROKECOLOR_RADIUS), new Object[]{radius.getSource()});
            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxUtils.hexString(Const.FILLCOLOR_RADIUS), new Object[]{radius.getSource()});
        }
    }

    /**
     * Сброс стиля ячеек на стандартный
     * @param objects Масив вершин
     */
    private void resetStyleCells(Object[] objects) {

        graph.setCellStyles(mxConstants.STYLE_FONTSIZE, Const.FONT_SIZE_DEF, objects);
        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(Const.STROKECOLOR_DEF), objects);
        graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxUtils.hexString(Const.FILLCOLOR_DEF), objects);
        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, mxUtils.hexString(Const.FONTCOLOR_DEF), objects);
        graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, Const.STROKEWIDTH_DEF, objects);

    }

    /**
     * Добавляет вершину на форму
     */
    public void addVertex(String shape) {
        graph.getModel().beginUpdate();
        try {
            int x = (int) (Math.random() * (Const.FRAME_WIDTH - 2 * Const.VERTEX_WIDTH));
            int y = (int) (Math.random() * (Const.FRAME_HEIGHT - 2 * Const.VERTEX_HEIGHT));
            mxCell cell = new mxCell("", new mxGeometry(x, y, Const.VERTEX_WIDTH, Const.VERTEX_HEIGHT), shape);
            cell.setVertex(true);
            graph.addCell(cell);
        }
        finally {
            graph.getModel().endUpdate();
        }
    }

    /**
     * Удаляет вершину или грань графа
     */
    public void deleteCell() {
        mxGraphActions.getDeleteAction().actionPerformed(new ActionEvent(getGraphComponent(), 0, ""));
    }

    /**
     * Изменяет цвет
     * @param color
     */
    public void changeColor(Color color) {
        mxCell cell = (mxCell) graph.getSelectionCell();
        graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxUtils.hexString(color), new Object[]{cell});
    }

    /**
     * Изменяет цвет обводки
     * @param color
     */
    public void changeStrokeColor(Color color) {
        mxCell cell = (mxCell) graph.getSelectionCell();
        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(color), new Object[]{cell});
    }

    public mxGraphComponent getGraphComponent() {
        return graphComponent;
    }

    /**
     * Показывает текстовое представление графа
     */
    public void getEncodeGraph() {
        String content = mxGdCodec.encode(graph).getDocumentString();

        System.out.println("Encode:\n" + content);
    }

    /**
     * Сохраняет граф в файл
     * @param filename Имя файла с графом
     * @throws IOException
     */
    public void saveToFile(String filename) throws IOException {
        String content = mxGdCodec.encode(graph).getDocumentString();
        mxUtils.writeFile(content, filename);
    }

    /**
     * Открывает граф из файла
     * @param file Файл с графом
     * @throws IOException
     */
    public void openFile(File file) throws IOException {
        mxGdDocument document = new mxGdDocument();
        document.parse(mxUtils.readFile(file.getAbsolutePath()));
        openGD(file, document);
        countVertex = 0;
    }

    /**
     * @throws IOException Ошибка
     *
     */
    private void openGD(File file, mxGdDocument document) {

        // Replaces file extension with .mxe
        String filename = file.getName();
        filename = filename.substring(0, filename.length() - 4) + ".mxe";

        if (new File(filename).exists()
                && JOptionPane.showConfirmDialog(getGraphComponent(),
                mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
            return;
        }

        ((mxGraphModel) graph.getModel()).clear();
        mxGdCodec.decode(document, graph);
        parent = graph.getDefaultParent();
        getGraphComponent().zoomAndCenter();

        onChange();
    }

    /**
     * Очистка графа
     */
    public void clear() {
        ((mxGraphModel) graph.getModel()).clear();
        parent = graph.getDefaultParent();
        countVertex = 0;
    }

    /**
     * Выход
     */
    public void exit() {
        frame.dispose();
    }

}