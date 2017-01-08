package mstb;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class LinearDistanceInteractiveMouseHandler implements MouseMotionListener, MouseListener {
	LinearDistanceInteractiveHandler parent = null;
	
	public LinearDistanceInteractiveMouseHandler (LinearDistanceInteractiveHandler Parent){
		parent = Parent;
	}

	public void mouseMoved(MouseEvent e) {
		parent.drawOverlay();
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			parent.addPoint();
			e.consume();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			parent.removePoint();
			e.consume();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		parent.drawOverlay();
	}

	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}
}
