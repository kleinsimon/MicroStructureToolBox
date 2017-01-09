package mstb.ldi;
import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import ij.ImagePlus;
import ij.gui.YesNoCancelDialog;
import mstb.ExclusiveOverlayMenuStrip;
import mstb.Tools;

public class LinearDistanceInteractiveMenuStrip extends Panel implements ActionListener, ItemListener, ExclusiveOverlayMenuStrip {
	private static final long serialVersionUID = 1L;
	private Label infoLabel, countLabel;
	private Button okButton, cancelButton, clearButton;
	private java.awt.Choice colorSelect;
	public LinearDistanceInteractiveHandler interactionHandler;
	public LinearDistanceInteractiveSettings settings;
	
	public LinearDistanceInteractiveMenuStrip(ImagePlus image, LinearDistanceInteractiveSettings settings) {
		this.settings = settings;
		
		infoLabel = new Label();
		infoLabel.setText("Left: Add. Right: Remove.");

		countLabel = new Label();
		countLabel.setText("Marks: 0");
		
		colorSelect = new java.awt.Choice();
		for (String c : ij.plugin.Colors.colors)
			colorSelect.add(c);
		colorSelect.select(settings.overlayColor);
		colorSelect.addItemListener(this);

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
		this.add(colorSelect);
		this.add(okButton);
		this.add(cancelButton);
		this.add(clearButton);
		
		interactionHandler = new LinearDistanceInteractiveHandler(image, settings, this);
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s == "OK") {
			interactionHandler.analyze();
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
			interactionHandler.clear();
	}

	public Boolean remove() {
		if (confirm("Cancel measurement? This will delete all marks.")) {
			interactionHandler.remove();
			this.getParent().remove(this);
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
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED)
			return;
		String s = (String) e.getItem();
		if (e.getSource() == colorSelect) {
			interactionHandler.setColor(Tools.getColorByName(s));
			interactionHandler.drawOverlay();
		}
	}
}
