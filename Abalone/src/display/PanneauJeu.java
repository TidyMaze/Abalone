package display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import objects.Case;
import objects.Plateau;

public class PanneauJeu extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Plateau plateau;
	public static final int DIMBOULE = 50;
	static BoutonRond tableauBoutons[][];

	public PanneauJeu(Plateau p) {

		class listenerAnnuler extends MouseAdapter {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					for (int i = 0; i < Plateau.HEIGHT; i++) {
						for (int j = 0; j < Plateau.WIDTH; j++) {
							BoutonRond bout = PanneauJeu.tableauBoutons[i][j];
							if (bout != null) {
								bout.setVisible(true);
								bout.setCouleurActuelle(null);
							}
						}
					}
			}

		}

		this.addMouseListener(new listenerAnnuler());

		tableauBoutons = new BoutonRond[Plateau.HEIGHT][Plateau.WIDTH];
		this.setPlateau(p);
		this.setLayout(null);

		for (int i = 0; i < Plateau.HEIGHT; i++) {
			for (int j = 0; j < Plateau.WIDTH; j++) {
				Case caseCourante = getPlateau().getCase(i, j);
				if (caseCourante != null) {
					BoutonRond tmpBouton = new BoutonRond(DIMBOULE, i, j);
					this.add(tmpBouton);
					tableauBoutons[i][j] = tmpBouton;
				}

			}
		}

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// parcours du tableau
		for (int i = 0; i < Plateau.HEIGHT; i++) {
			for (int j = 0; j < Plateau.WIDTH; j++) {
				Case caseCourante = getPlateau().getCase(i, j);
				// case existe ?
				if (caseCourante != null) {
					// case occupee ?
					if (caseCourante.estOccupee()) {

						g.setColor(Color.BLACK);
						g.fillOval(j * DIMBOULE + i * DIMBOULE / 2, i
								* (DIMBOULE - DIMBOULE / 8), DIMBOULE, DIMBOULE);

						// selon la couleur
						switch (caseCourante.getBoule().getCouleur()) {
						case NOIR:
							g.setColor(Color.DARK_GRAY);
							break;
						case BLANC:
							g.setColor(Color.WHITE);
							break;
						default:
						}
						g.fillOval(j * DIMBOULE + i * DIMBOULE / 2, i
								* (DIMBOULE - DIMBOULE / 8), DIMBOULE, DIMBOULE);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.fillOval(j * DIMBOULE + i * DIMBOULE / 2, i
								* (DIMBOULE - DIMBOULE / 8), DIMBOULE, DIMBOULE);
					} // fin case occup�e

				} // fin case existe

				/*
				 * else { g.setColor(Color.DARK_GRAY); }
				 */

			}
		}

	}

	public static Plateau getPlateau() {
		return plateau;
	}

	public void setPlateau(Plateau plateau) {
		PanneauJeu.plateau = plateau;
	}
}
