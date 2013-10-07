package abalone;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class PanneauJeu extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Plateau plateau;
	public static final int DIMBOULE = 50;

	public PanneauJeu(Plateau p) {

		this.plateau = p;
		this.setLayout(null);

		for (int i = 0; i < Plateau.HEIGHT; i++) {
			for (int j = 0; j < Plateau.WIDTH; j++) {
				Case caseCourante = plateau.getCase(i, j);
				// case existe ?
				if (caseCourante != null) {
					BoutonRond tmpBouton = new BoutonRond(DIMBOULE, j
							* DIMBOULE + i * DIMBOULE / 2, i
							* (DIMBOULE - DIMBOULE / 8));
					// case occup�e ?
					if (caseCourante.estOccupee()) {
						// selon la couleur
						switch (caseCourante.getBoule().getCouleur()) {
						case NOIR:
							tmpBouton.setColor(Color.DARK_GRAY);
							break;
						case BLANC:
							tmpBouton.setColor(Color.WHITE);
							break;
						default:
						}

					} else {
						tmpBouton.setColor(Color.LIGHT_GRAY);
					} // fin case occup�e
					this.add(tmpBouton);

				} // fin case existe

				/*
				 * else { g.setColor(Color.DARK_GRAY); }
				 */

			}
		}

	}

	public void paintComponent_old(Graphics g) {
		super.paintComponent(g);
		// parcours du tableau
		for (int i = 0; i < Plateau.HEIGHT; i++) {
			for (int j = 0; j < Plateau.WIDTH; j++) {
				Case caseCourante = plateau.getCase(i, j);
				// case existe ?
				if (caseCourante != null) {
					// case occup�e ?
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
						g.fillOval(j * DIMBOULE + i * DIMBOULE / 2 - 3, i
								* (DIMBOULE - DIMBOULE / 8) - 3, DIMBOULE,
								DIMBOULE);
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
}