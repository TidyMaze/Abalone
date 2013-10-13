import core.Partie;
import display.FenetreAbalone;

public class Application {

	public static void main(String[] args) {
		FenetreAbalone fenetre = new FenetreAbalone();
		Partie laPartie = new Partie(fenetre);
		fenetre.setTitle("Abalone");
		fenetre.setVisible(true);
		laPartie.lancerPartie();
	}
}
