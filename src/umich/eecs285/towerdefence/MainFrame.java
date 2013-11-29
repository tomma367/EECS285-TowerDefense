package umich.eecs285.towerdefence;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class MainFrame extends JFrame implements TowerDefensedataArray {

  private MainPanel mP;
  private PlayerPanel pp;

  MainFrame() {

    super("Tower Defence");
    this.setSize(1200, 900);
    this.setResizable(false);
    this.setLayout(null);

    pp = new PlayerPanel();
    add(pp);

    mP = new MainPanel(pp);
    add(mP);

    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (pp.CreateIndex() != -1) {
          System.out.println(pp.CreateIndex());
          pp.CreateButtonRecover();
        }
      }
    });
  }

  public void nextFrame(TowerDefense_TransData t) {
    mP.nextPanel(t);

  }

}
