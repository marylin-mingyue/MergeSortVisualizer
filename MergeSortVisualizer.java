import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class MergeSortVisualizer extends JPanel {
    private int[] array;     
    private int[] tempArray;
    private int highlightA = -1;
    private int highlightB = -1;
    private int delay = 50;

    public MergeSortVisualizer(int size) {
        setPreferredSize(new Dimension(800, 500));
        initArray(size);
    }

    private void initArray(int size) {
        array = new int[size];
        tempArray = new int[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = rand.nextInt(getPreferredSize().height - 50) + 50;
        }
        highlightA = highlightB = -1;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / array.length;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < array.length; i++) {
            if (i == highlightA || i == highlightB) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.WHITE);
            }
            int x = i * barWidth;
            int y = height - array[i];
            g.fillRect(x, y, barWidth - 2, array[i]);
        }
    }
    public void startSort() {
        new Thread(() -> {
            try {
                mergeSort(0, array.length - 1);
                highlightA = highlightB = -1;
                repaint();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void mergeSort(int left, int right) throws InterruptedException {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(left, mid);
            mergeSort(mid + 1, right);
            merge(left, mid, right);
        }
    }
    private void merge(int left, int mid, int right) throws InterruptedException {
        System.arraycopy(array, left, tempArray, left, right - left + 1);

        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            highlightA = i;
            highlightB = j;
            if (tempArray[i] <= tempArray[j]) {
                array[k++] = tempArray[i++];
            } else {
                array[k++] = tempArray[j++];
            }
            repaint();
            Thread.sleep(delay);
        }
        while (i <= mid) {
            highlightA = i;
            highlightB = -1;
            array[k++] = tempArray[i++];
            repaint();
            Thread.sleep(delay);
        }
        while (j <= right) {
            highlightA = -1;
            highlightB = j;
            array[k++] = tempArray[j++];
            repaint();
            Thread.sleep(delay);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Merge Sort 可视化");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            MergeSortVisualizer panel = new MergeSortVisualizer(80);
            JButton startBtn = new JButton("开始排序");
            JButton resetBtn = new JButton("重新生成");
            JSlider speedSlider = new JSlider(1, 200, panel.delay);
            speedSlider.setMajorTickSpacing(50);
            speedSlider.setMinorTickSpacing(10);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);

            startBtn.addActionListener((ActionEvent e) -> {
                startBtn.setEnabled(false);
                resetBtn.setEnabled(false);
                panel.startSort();
                // 排序结束后再重新启用按钮
                new Thread(() -> {
                    try {
                        // 最坏情况下大约需要 n·log(n) 步，每步 delay 耗时
                        Thread.sleep((long)(panel.array.length * Math.log(panel.array.length) / Math.log(2) * panel.delay * 2));
                    } catch (InterruptedException ex) { }
                    SwingUtilities.invokeLater(() -> {
                        startBtn.setEnabled(true);
                        resetBtn.setEnabled(true);
                    });
                }).start();
            });

            resetBtn.addActionListener((ActionEvent e) -> {
                panel.initArray(panel.array.length);
            });

            speedSlider.addChangeListener(e -> panel.delay = speedSlider.getValue());

            JPanel controls = new JPanel();
            controls.add(startBtn);
            controls.add(resetBtn);
            controls.add(new JLabel("速度(ms):"));
            controls.add(speedSlider);

            frame.setLayout(new BorderLayout());
            frame.add(controls, BorderLayout.NORTH);
            frame.add(panel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}