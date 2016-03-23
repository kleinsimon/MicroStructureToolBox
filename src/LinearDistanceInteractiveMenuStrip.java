import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ij.ImagePlus;
import ij.gui.YesNoCancelDialog;
import ij.measure.ResultsTable;

public class LinearDistanceInteractiveMenuStrip extends Panel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Label infoLabel, countLabel;
	private Button okButton, cancelButton, clearButton;
	public LinearDistanceInteractiveHandler parentHandle;
	private Boolean clearConf = false;
	private Boolean removeConf = false;

	public LinearDistanceInteractiveMenuStrip(int lineDistancePx, int offSetPx, int markLenPx, Boolean dirY,
			ImagePlus image, String[] resTable, boolean[] doRes, ResultsTable restable) {

		infoLabel = new Label();
		infoLabel.setText("Left: Add. Right: Remove.");

		countLabel = new Label();
		countLabel.setText("Marks: 0");

		okButton = new Button();
		okButton.setLabel("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		cancelButton = new Button();
		cancelButton.setLabel("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);

		clearButton = new Button();
		clearButton.setLabel("Clear");
		clearButton.setActionCommand("Clear");
		clearButton.addActionListener(this);

		this.add(infoLabel);
		this.add(countLabel);
		this.add(okButton);
		this.add(cancelButton);
		this.add(clearButton);
		
		parentHandle = new LinearDistanceInteractiveHandler(lineDistancePx, offSetPx,
				markLenPx, dirY, image, resTable, doRes, restable, this);
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s == "OK") {
			parentHandle.analyze();
		} else if (s == "Cancel") {
			remove();
		} else if (s == "Clear") {
			clear();
		}
	}

	public void setCounts(Integer count) {
		countLabel.setText("Marks: " + count.toString());
	}

	private void clear() {
		if (confirm("Really clear marks?"))
			parentHandle.clear();
	}

	public boolean remove() {
		if (confirm("Cancel measurement? THis will delete all marks.")) {
			parentHandle.remove();
			return true;
		}
		return false;
	}

	public Boolean confirm(String Message) {
		YesNoCancelDialog dg = new YesNoCancelDialog(null, "Confirm", Message);
		if (dg.yesPressed())
			return true;
		else
			return false;
	}
}
