package umich.eecs285.towerdefence;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class EarningPanel extends JPanel {
  private int numberOfMeoMeo;
  
  EarningPanel() {
    setBounds(0, 500, 150, 250);
    setLayout(null);
    
    numberOfMeoMeo = 1;
    
    TowerDefense_Button defaultMeoMeo = new TowerDefense_Button("res/meomeo.png", "res/meomeo.png");
    defaultMeoMeo.setBounds(10, 70, MainFrame.ButtonSize, MainFrame.ButtonSize);
    add(defaultMeoMeo);
    
    TowerDefense_Button defaultMoney = new TowerDefense_Button("res/money.png", "res/money.png");
    defaultMoney.setBounds(10, 10, MainFrame.ButtonSize, MainFrame.ButtonSize);
    add(defaultMoney);
    
  }
  
  public void addMeoMeo() {
    TowerDefense_Button newMeoMeo = new TowerDefense_Button("res/alien.gif", "res/alien.gif");
    newMeoMeo.setBounds(10, 10 + 40 * numberOfMeoMeo, MainFrame.ButtonSize, MainFrame.ButtonSize);
    add(newMeoMeo);
    repaint();
    
    ++numberOfMeoMeo;
  }
  

  public void paintComponent(Graphics g) {
    ImageIcon Icon = new ImageIcon("res/cat.jpg");
    g.drawImage(Icon.getImage(), 0, 0, getSize().width, getSize().height, this);
  }

}
