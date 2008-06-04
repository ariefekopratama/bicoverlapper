package es.usal.bicoverlapper.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class ProgressBarDemo extends JPanel implements ActionListener {
  public final static int ONE_SECOND = 1000;
  private JProgressBar progressBar;
  private Timer timer;
  private JButton startButton;
  private LongTask task;
  private JTextArea taskOutput;
  private String newline = "\n";
  public ProgressBarDemo() {
      super(new BorderLayout());
      task = new LongTask();
      startButton = new JButton("Start");
      startButton.setActionCommand("start");
      startButton.addActionListener(this);
      progressBar = new JProgressBar(0, task.getLengthOfTask());
      progressBar.setValue(0);
      progressBar.setStringPainted(true);
      taskOutput = new JTextArea(5, 20);
      taskOutput.setMargin(new Insets(5,5,5,5));
      taskOutput.setEditable(false);
      taskOutput.setCursor(null);
      JPanel panel = new JPanel();
      panel.add(startButton);
      panel.add(progressBar);
      add(panel, BorderLayout.PAGE_START);
      add(new JScrollPane(taskOutput), BorderLayout.CENTER);
      setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      timer = new Timer(ONE_SECOND, new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              progressBar.setValue(task.getCurrent());
              String s = task.getMessage();
              if (s != null) {
                  taskOutput.append(s + newline);
                  taskOutput.setCaretPosition(
                          taskOutput.getDocument().getLength());
              }
              if (task.isDone()) {
                  Toolkit.getDefaultToolkit().beep();
                  timer.stop();
                  startButton.setEnabled(true);
                  setCursor(null); //turn off the wait cursor
                  progressBar.setValue(progressBar.getMinimum());
              }
          }
      });
  }
  public void actionPerformed(ActionEvent evt) {
      startButton.setEnabled(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      task.go();
      timer.start();
  }
  private static void createAndShowGUI() {
      JFrame.setDefaultLookAndFeelDecorated(true);
      JFrame frame = new JFrame("ProgressBarDemo");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JComponent newContentPane = new ProgressBarDemo();
      newContentPane.setOpaque(true); //content panes must be opaque
      frame.setContentPane(newContentPane);
      frame.pack();
      frame.setVisible(true);
  }
  public static void main(String[] args) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
              createAndShowGUI();
          }
      });
  }
}
class LongTask {
  private int lengthOfTask;
  private int current = 0;
  private boolean done = false;
  private boolean canceled = false;
  private String statMessage;

  public LongTask() {
      lengthOfTask = 1000;
  }
  public void go() {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
              current = 0;
              done = false;
              canceled = false;
              statMessage = null;
              return new ActualTask();
          }
      };
      worker.start();
  }
  public int getLengthOfTask() {
      return lengthOfTask;
  }
  public int getCurrent() {
      return current;
  }
  public void stop() {
      canceled = true;
      statMessage = null;
  }
  public boolean isDone() {
      return done;
  }
  public String getMessage() {
      return statMessage;
  }
  class ActualTask {
      ActualTask() {
          while (!canceled && !done) {
              try {
                  Thread.sleep(1000);
                  current += Math.random() * 100;
                  if (current >= lengthOfTask) {
                      done = true;
                      current = lengthOfTask;
                  }
                  statMessage = "Completed " + current +
                                " out of " + lengthOfTask + ".";
              } catch (InterruptedException e) {
                  System.out.println("ActualTask interrupted");
              }
          }
      }
  }
}

abstract class SwingWorker {
  private Object value;
  private static class ThreadVar {
      private Thread thread;
      ThreadVar(Thread t) { thread = t; }
      synchronized Thread get() { return thread; }
      synchronized void clear() { thread = null; }
  }
  private ThreadVar threadVar;
  protected synchronized Object getValue() {
      return value;
  }
  private synchronized void setValue(Object x) {
      value = x;
  }
  public abstract Object construct();
  public void finished() {
  }

  public void interrupt() {
      Thread t = threadVar.get();
      if (t != null) {
          t.interrupt();
      }
      threadVar.clear();
  }
  public Object get() {
      while (true) {
          Thread t = threadVar.get();
          if (t == null) {
              return getValue();
          }
          try {
              t.join();
          }
          catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return null;
          }
      }
  }
  public SwingWorker() {
      final Runnable doFinished = new Runnable() {
         public void run() { finished(); }
      };

      Runnable doConstruct = new Runnable() {
          public void run() {
              try {
                  setValue(construct());
              }
              finally {
                  threadVar.clear();
              }

              SwingUtilities.invokeLater(doFinished);
          }
      };

      Thread t = new Thread(doConstruct);
      threadVar = new ThreadVar(t);
  }
  public void start() {
      Thread t = threadVar.get();
      if (t != null) {
          t.start();
      }
  }
}
