package controller;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.sun.istack.internal.NotNull;
import jdk.nashorn.internal.runtime.options.Option;
import model.Ball;
import model.Board;
import model.Color;
import model.Direction;
import model.Game;
import model.Player;
import model.Space;
import utils.Coord;
import utils.CoordDouble;
import utils.Vector;
import view.RoundButton;
import view.GamePanel;
import view.Window;

public class GameController {

  public static final int CLICGAUCHE = MouseEvent.BUTTON1;
  public static final int CLICDROIT = MouseEvent.BUTTON3;
  public static final int MAX_DEPLACEMENTS = 3;
  public static final int ips = 35;
  public static final int temps = 100;


  private Optional<Coord> maybeB1 = Optional.empty();
  private Optional<Coord> maybeB2 = Optional.empty();
  private Optional<Coord> maybeB3 = Optional.empty();

  private Window fenetre;
  public Game modele;
  private State etat;

  // private Coord sensDeuxBoules;

  public Optional<Coord> getB1() { return maybeB1; }

  public Optional<Coord> getB2() {
    return maybeB2;
  }

  public Optional<Coord> getB3() {
    return maybeB3;
  }

  public void setState(controller.State etat) {
    System.out.println("Transition from state " + this.etat + " to " + etat);
    this.etat = etat;
  }

  public State getEtat() {
    return this.etat;
  }

  public GameController(Game modele) {
    this.modele = modele;
  }

  public void afficherB1B2B3() {
    System.out.println("b1 : " + maybeB1 +  " b2 : " + maybeB2 + " b3 : " + maybeB3);
  }

  public void cleanBalls() {
    maybeB1 = Optional.empty();
    maybeB2 = Optional.empty();
    maybeB3 = Optional.empty();
    System.out.println("dans viderB1B2B3");
    afficherB1B2B3();
  }

  public int compteCouleur(Direction delta, Coord depart, Color couleur) {
    return this.compteCouleur(delta, depart.y, depart.x, couleur);
  }

  public int compteCouleur(Direction delta, int iDep, int jDep, Color couleur) {
    int nbCoul = 0;
    Coord parcours = new Coord(jDep, iDep);
    Board plateau = modele.getPlateau();
    while (plateau.getSpace(parcours) != null && plateau.getSpace(parcours).estOccupee()
        && plateau.getSpace(parcours).ball.color == couleur) {
      nbCoul++;
      parcours.x = parcours.x + delta.vector.x;
      parcours.y = parcours.y + delta.vector.y;
    }
    return nbCoul;
  }

  public void verifierBoules() {
    Board board = modele.getPlateau();
    // verification boule hors jeu
    for (int i = 0; i < board.height; i++) {
      for (int j = 0; j < board.width; j++) {
        if (board.getSpace(i, j).isBorder && board.getSpace(i, j).estOccupee()) {
          for (Player joueur : modele.players) {
            if (board.getSpace(i, j).ball.color == joueur.color) {
              joueur.setBoulesDuJoueurEjectees(joueur.getBoulesDuJoueurEjectees() + 1);
            }
          }
          board.getSpace(i, j).ball = null;
          modele.verifierVictoire();
        }
      }
    }
  }

  public void sourisRelachee(MouseEvent e) {

    RoundButton bouton = ((RoundButton) e.getSource());
    GamePanel panneau = fenetre.getPanel();
    Board plateau = modele.getPlateau();

    System.out.println("clic : ligne " + bouton.getCoordI() + ", colonne " + bouton.getCoordJ());

    Direction[] lesDir = Direction.values();

    // BoutonRond tmp; /* tous les boutons temporaires de parcours
    // circulaire */

    switch (etat) {
      case NORMAL:
        RoundButton tmp;
        switch (e.getButton()) {
          case CLICGAUCHE: handleLeftClickInNormalState(bouton, panneau, plateau, lesDir); break;
          case CLICDROIT: handleRightClickInNormalState(bouton, panneau, plateau, lesDir); break;
          default: break;
        }
        break;
      case FIRST_SELECTED_FOR_LINE:
        switch (e.getButton()) {
          case CLICGAUCHE: handleLeftClickInFirstSelectedForLineState(bouton, panneau, plateau); break;
          default: break;
        }
        break;
      case FIRST_SELECTED_FOR_LATERAL:
        switch (e.getButton()) {
          case CLICDROIT: handleRightClickInFirstSelectedForLateralState(bouton, panneau, plateau); break;
          default: break;
        }
        break;
      case SECOND_SELECTED_FOR_LATERAL:
        switch (e.getButton()) {
          case CLICGAUCHE: handleLeftClickInsecondSelectedForLateralState(bouton, panneau, plateau); break;
          case CLICDROIT: handleRightClickInSecondSelectedForLateralState(bouton, plateau); break;
          default: break;
        }
        break;
      case THIRD_SELECTED_FOR_LATERAL:
        switch (e.getButton()) {
          case CLICGAUCHE: handleLeftClickInThirdSelectedfForLateralState(); break;
          default: break;
        }
        break;
      default:
        break;

    }
  }

  private void handleLeftClickInThirdSelectedfForLateralState() {
    cleanBalls();
    setState(State.NORMAL);
  }

  private void handleRightClickInSecondSelectedForLateralState(RoundButton bouton, Board plateau) {
    if (bouton.isSelectionne())
      return;

    maybeB3 = Optional.of(bouton.coord);

    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
    Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));
    Coord b3 = maybeB3.orElseThrow(() -> new NoSuchElementException("B3 was not set!"));

    reordonne(b1, b2, b3);
    afficherB1B2B3();

    Space caseB1 = plateau.getSpace(b1);
    Space caseB2 = plateau.getSpace(b2);
    Space caseB3 = plateau.getSpace(b3);

    Vector sensDeuxBoules = new Vector(b1, b2);

    Space caseDecalDepart = plateau.getSpace(b1.add(sensDeuxBoules.getOpposite()));
    Space caseDecalArrivee = plateau.getSpace(b3.add(sensDeuxBoules));

    HashSet<Space> casesSpeciales = new HashSet<>();
    casesSpeciales.add(caseDecalDepart);
    casesSpeciales.add(caseDecalArrivee);
    casesSpeciales.add(caseB1);
    casesSpeciales.add(caseB3);
    casesSpeciales.add(caseB2);

    /* affichage des boutons lateraux */
    cercleBoutonsLateraux(casesSpeciales, caseB1);
    cercleBoutonsLateraux(casesSpeciales, caseB2);
    cercleBoutonsLateraux(casesSpeciales, caseB3);

    caseDecalDepart.bouton.reset();
    caseDecalArrivee.bouton.reset();

    eliminerForeverAlone();

    setState(State.THIRD_SELECTED_FOR_LATERAL);
  }

  private void handleLeftClickInsecondSelectedForLateralState(RoundButton bouton, GamePanel panneau, Board plateau) {
    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
    Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));
    Vector sensDeuxBoules = new Vector(b1, b2);

    Space caseProlonge = plateau.getSpace(bouton.coord.add(sensDeuxBoules));
    Space caseInverse = plateau.getSpace(bouton.coord.add(sensDeuxBoules.getOpposite()));

    if ((caseProlonge.bouton != null && !caseProlonge.isBorder
        && caseProlonge.bouton.isMouseOver() && !caseProlonge.estOccupee())
        || (caseInverse.bouton != null && !caseInverse.isBorder
            && caseInverse.bouton.isMouseOver() && !caseInverse.estOccupee()
            && caseInverse.bouton.isCliquableGauche())) {

      Vector sensDeplac = new Vector(b1, bouton.coord);
      int nbBoules = 2;
      int periode = temps / nbBoules / ips;

      if (!caseProlonge.bouton.isCliquableGauche()) {
        sensDeplac = sensDeplac.add(sensDeuxBoules.getOpposite());
      }
      panneau.hideButtons();
      try {
        deplacerBouleDirection(Direction.toDirection(sensDeplac), b1, periode);
        deplacerBouleDirection(Direction.toDirection(sensDeplac), b2, periode);
      } catch (MovementException e1) {
        e1.printStackTrace();
      }

      cleanBalls();
      panneau.hideButtons();
      panneau.updateClickables();

      setState(State.NORMAL);
    }
  }

  private void handleRightClickInFirstSelectedForLateralState(RoundButton bouton, GamePanel panneau, Board plateau) {
    if (bouton.isSelectionne())
      return;

    maybeB2 = Optional.of(bouton.getCoord());
    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
    Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));

    Vector sensDeuxBoules = new Vector(b1, b2);

    panneau.hideButtons();

    Space caseDepart = plateau.getSpace(b1);
    Space caseArrivee = plateau.getSpace(b2);
    Space caseDecalDepart = plateau.getSpace(b1.add(sensDeuxBoules.getOpposite()));
    Space caseDecalArrivee = plateau.getSpace(b2.add(sensDeuxBoules));

    HashSet<Space> listeCases = new HashSet<>();
    listeCases.add(caseDepart);
    listeCases.add(caseArrivee);
    listeCases.add(caseDecalDepart);
    listeCases.add(caseDecalArrivee);

    /* affichage des boutons lateraux */
    cercleBoutonsLateraux(listeCases, caseDepart);
    cercleBoutonsLateraux(listeCases, caseArrivee);

    /* affichage des boutons decal */

    if (caseDecalArrivee.estOccupee()
        && caseDecalArrivee.ball.color == caseArrivee.ball.color) {
      caseDecalArrivee.bouton.mettreCliquableDroit();
    } else {
      caseDecalArrivee.bouton.reset();
    }

    if (caseDecalDepart.estOccupee()
        && caseDecalDepart.ball.color == caseDepart.ball.color) {
      caseDecalDepart.bouton.mettreCliquableDroit();
    } else {
      caseDecalDepart.bouton.reset();
    }

    eliminerForeverAlone();

    setState(State.SECOND_SELECTED_FOR_LATERAL);
  }

  private void handleLeftClickInFirstSelectedForLineState(RoundButton bouton, GamePanel panneau, Board plateau) {
    maybeB2 = Optional.of(bouton.getCoord());

    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
    Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));
    Vector sensDeuxBoules = new Vector(b1, b2);
    Direction dir = Direction.toDirection(sensDeuxBoules);

    /* premiere ligne de boules */
    Color couleurDepart = plateau.getSpace(b1).ball.color;
    int nbCouleurActuelle = this.compteCouleur(dir, b1, couleurDepart);
    int nbCouleurOpposee = 0;

    Space caseDeFinCouleurActuelle =
        plateau.getSpace(b1.y + nbCouleurActuelle * sensDeuxBoules.y,
            b1.x + nbCouleurActuelle * sensDeuxBoules.x);

    /* seconde ligne de boules */
    if (caseDeFinCouleurActuelle.estOccupee()) {
      Color couleurArr = caseDeFinCouleurActuelle.ball.color;
      nbCouleurOpposee = compteCouleur(dir, b1.y + nbCouleurActuelle * sensDeuxBoules.y,
          b1.x + nbCouleurActuelle * sensDeuxBoules.x, couleurArr);
    }
    int nbBoulesDeplac = nbCouleurActuelle + nbCouleurOpposee;
    boolean deplacementPossible = true;

    // verification de la case qui suit la derniere
    Space caseFinale = plateau.getSpace(b1.y + nbBoulesDeplac * sensDeuxBoules.y,
        b1.x + nbBoulesDeplac * sensDeuxBoules.x);
    if (caseFinale != null && caseFinale.estOccupee()) {
      deplacementPossible = false;
    }

    System.out.println("nbCouleurAcuelle = " + nbCouleurActuelle + ", nbCouleurOpposee = "
        + nbCouleurOpposee);

    // deplacement reel
    if (nbCouleurActuelle <= MAX_DEPLACEMENTS && nbCouleurOpposee < nbCouleurActuelle
        && deplacementPossible) {
      panneau.hideButtons();
      this.deplacerLigneBoules(nbBoulesDeplac, sensDeuxBoules);
      verifierBoules();
    }

    /* nettoyage et reaffichage */
    cleanBalls();
    panneau.hideButtons();
    panneau.updateClickables();

    setState(State.NORMAL);
  }

  private void handleRightClickInNormalState(RoundButton bouton, GamePanel panneau, Board plateau, Direction[] lesDir) {
    RoundButton tmp;// coordonnees depart

    maybeB1 = Optional.of(bouton.getCoord());
    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));

    System.out.println("dans 1er clic droit");
    afficherB1B2B3();
    System.out.println(bouton);

    panneau.hideButtons();

    RoundButton boutonDep = plateau.getSpace(b1).bouton;
    boutonDep.setVisible(true);

    // afficher cercle voisins
    for (Direction dir : lesDir) {
      Space spaceDest = plateau.getSpace(bouton.getCoordI() + dir.vector.y,
          bouton.getCoordJ() + dir.vector.x);

      tmp = spaceDest.bouton;
      if (tmp != null && !spaceDest.isBorder && spaceDest.estOccupee()
          && spaceDest.ball.color == plateau.getSpace(b1).ball.color) {
        tmp.mettreCliquableDroit();
      }
    }

    setState(State.FIRST_SELECTED_FOR_LATERAL);
  }

  private void handleLeftClickInNormalState(RoundButton bouton, GamePanel panneau, Board plateau, Direction[] lesDir) {
    RoundButton tmp;// coordonnees depart
    maybeB1 = Optional.of(bouton.getCoord());
    setState(State.FIRST_SELECTED_FOR_LINE);

    // cacher
    panneau.hideButtons();

    // afficher cercle voisins
    for (Direction dir : lesDir) {
      Coord dest =
          new Coord(bouton.getCoordJ() + dir.vector.x, bouton.getCoordI() + dir.vector.y);

      tmp = plateau.getSpace(dest).bouton;
      if (tmp != null && !plateau.getSpace(dest).isBorder) {
        tmp.mettreCliquableGauche();
      }
    }
  }

  private void reordonne(Coord p, Coord s, Coord t) {
    Vector sToT = new Vector(s, t);
    if (Math.abs(sToT.x) > 1 || Math.abs(sToT.y) > 1) {
      Coord pSave = new Coord(p);
      Coord sSave = new Coord(s);
      Coord tSave = new Coord(t);
      p.setCoord(tSave);
      s.setCoord(pSave);
      t.setCoord(sSave);
    }
  }


  // Met en cliquable gauche les boutons qui ne sont pas dans listeCases et qui entourent centre
  private void cercleBoutonsLateraux(HashSet<Space> blackList, Space centre) {
    Board plateau = this.modele.getPlateau();
    Direction[] lesDir = Direction.values();
    for (Direction dir : lesDir) {
      Space caseDest = plateau.getSpace(centre.bouton.getCoordI() + dir.vector.y,
          centre.bouton.getCoordJ() + dir.vector.x);
      RoundButton tmp = caseDest.bouton;
      if (tmp != null && !caseDest.isBorder && !caseDest.estOccupee()) {
        for (Space caseTmp : blackList) {
          if (caseDest.bouton.equals(caseTmp.bouton))
            break;
        }
        tmp.mettreCliquableGauche();
      }
    }
  }

  private void eliminerForeverAlone() {
    Board plateau = this.modele.getPlateau();
    Direction[] lesDir = Direction.values();

    // cache les boutons isolés
    for (int i = 0; i < this.modele.getPlateau().height; i++) {
      for (int j = 0; j < this.modele.getPlateau().width; j++) {
        if (plateau.getSpace(i, j).bouton.isCliquableGauche()) {
          boolean aVoisin = false;
          for (Direction dir : lesDir) {
            if (plateau.getSpace(i + dir.vector.y, j + dir.vector.x).bouton.isCliquableGauche()) {
              aVoisin = true;
              break;
            }
          }
          if (!aVoisin) {
            plateau.getSpace(i, j).bouton.reset();
          }
        }
      }
    }
  }

  private void deplacerLigneBoules(int nbBoules, Vector delta) {
    int periode = temps / nbBoules / ips;

    Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));

    System.out.println(nbBoules + " boule(s) a deplacer");
    // la derniere boule est la premiere deplacee
    Coord coordDepla = new Coord(b1.x + (nbBoules - 1) * delta.x, b1.y + (nbBoules - 1) * delta.y);
    while (nbBoules > 0) {
      nbBoules--;
      try {
        deplacerBouleDirection(Direction.toDirection(delta), coordDepla, periode);

      } catch (MovementException e1) {
        e1.printStackTrace();
      }

      coordDepla.x = coordDepla.x - delta.x;
      coordDepla.y = coordDepla.y - delta.y;
    }
  }

  public void sourisEntree(MouseEvent e) {
    RoundButton bouton = ((RoundButton) e.getSource());
    bouton.setMouseOver(true);
    if (etat == State.SECOND_SELECTED_FOR_LATERAL) {
      Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
      Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));

      Vector sensDeuxBoules = new Vector(b1, b2);
      Coord coordDepla = bouton.coord.add(sensDeuxBoules);
      if (!modele.getPlateau().getSpace(coordDepla).bouton.isVisible()) {
        coordDepla = bouton.coord.add(sensDeuxBoules.getOpposite());
      }
      modele.getPlateau().getSpace(coordDepla).bouton.setMouseOver(true);
      fenetre.repaint();
    } else if (etat == State.THIRD_SELECTED_FOR_LATERAL) {
      // TODO On verra pus tard...
    }
  }

  public void sourisSortie(MouseEvent e) {
    RoundButton bouton = ((RoundButton) e.getSource());
    bouton.setMouseOver(false);
    if (etat == State.SECOND_SELECTED_FOR_LATERAL) {
      Coord b1 = maybeB1.orElseThrow(() -> new NoSuchElementException("B1 was not set!"));
      Coord b2 = maybeB2.orElseThrow(() -> new NoSuchElementException("B2 was not set!"));

      Vector sensDeuxBoules = new Vector(b1, b2);
      Coord coordDepla = bouton.coord.add(sensDeuxBoules);
      if (!modele.getPlateau().getSpace(coordDepla).bouton.isVisible()) {
        coordDepla = bouton.coord.add(sensDeuxBoules.getOpposite());
      }
      modele.getPlateau().getSpace(coordDepla).bouton.setMouseOver(false);
      fenetre.repaint();
    } else if (etat == State.THIRD_SELECTED_FOR_LATERAL) {
      // TODO On verra pus tard...
    }
  }

  public void setWindow(Window fenetre) {
    this.fenetre = fenetre;
  }

  public void deplacerBouleDirection(Direction dir, Coord coordCase, int tempsPeriode)
      throws MovementException {

    System.out.println("deplacement de (" + coordCase.x + ";" + coordCase.y + ") en direction ("
        + dir.vector.x + ";" + dir.vector.y + ")");

    if (coordCase.x < 0 || coordCase.x >= modele.getPlateau().width || coordCase.y < 0
        || coordCase.y >= modele.getPlateau().height) {
      throw new MovementException("case debut invalide (<0 | >" + modele.getPlateau().height + ")");
    }

    Space caseActuelle = modele.getPlateau().getSpace(coordCase);
    Coord coordCaseSuivante = modele.getPlateau().getNeighbor(coordCase, dir);

    if (!caseActuelle.estOccupee()) {
      throw new MovementException("case debut non occupee");
    }
    if (modele.getPlateau().getSpace(coordCaseSuivante).estOccupee()) {
      throw new MovementException("case arrivee occcupee");
    }

    Ball bouleADeplacer = caseActuelle.ball;

    CoordDouble delta =
        new CoordDouble((double) (dir.vector.x) / ips, (double) (dir.vector.y) / ips);

    for (int i = 0; i < ips; i++) {
      long debut = System.currentTimeMillis();
      bouleADeplacer.coord.setCoord(CoordDouble.somme(bouleADeplacer.coord, delta));

      fenetre.repaint();

      fenetre.getPanel().paintImmediately(0, 0, fenetre.getPanel().getWidth(),
          fenetre.getPanel().getHeight());

      try {
        long tpsRestant = tempsPeriode - (System.currentTimeMillis() - debut);
        if (tpsRestant > 0)
          Thread.sleep(tpsRestant);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }

    bouleADeplacer.coord.setCoord(coordCaseSuivante.y, coordCaseSuivante.x);
    modele.getPlateau().getSpace(coordCaseSuivante).ball = caseActuelle.ball;
    caseActuelle.ball = null;

  }
}