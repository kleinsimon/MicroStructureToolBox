import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class PointAnalysisInteractiveMouseHandler implements MouseMotionListener, MouseListener {
	PointAnalysisInteractiveHandler parent = null;
	
	public PointAnalysisInteractiveMouseHandler (PointAnalysisInteractiveHandler Parent){
		parent = Parent;
	}

	public void mouseMoved(MouseEvent e) {
		//parent.drawOverlay();
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			parent.togglePoint(false);
			e.consume();
		} else if(e.getButton() == MouseEvent.BUTTON3) {
			parent.togglePoint(true);
			e.consume();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		//parent.drawOverlay();
	}

	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mousePressed(MouseEvent e) {

	}
}
