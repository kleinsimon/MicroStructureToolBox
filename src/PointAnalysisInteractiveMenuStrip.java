import java.awt.Button;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.YesNoCancelDialog;
import ij.measure.ResultsTable;

public class PointAnalysisInteractiveMenuStrip extends Panel implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;
	private Label countLabel;
	private Button okButton, cancelButton, clearButton, thButton;
	private java.awt.Choice colorSelect, thSelect;
	private java.awt.TextField thValue;
	//private JSlider thSlider;
	public PointAnalysisInteractiveHandler interactionHandler;

	public PointAnalysisInteractiveMenuStrip(ImagePlus image, ResultsTable restable) {
		String overlayColor = Prefs.get("PointAnalysisInteractive.overlayColor", "Red");
		
		countLabel = new Label();
		countLabel.setText("");
		colorSelect = new java.awt.Choice();
		for (String c : ij.plugin.Colors.colors)
			colorSelect.add(c);
		colorSelect.select(overlayColor);
		colorSelect.addItemListener(this);
		
		thSelect = new java.awt.Choice();
		thSelect.add("H");
		thSelect.add("S");
		thSelect.add("B");
		thSelect.select("B");
		
		thButton = new Button();
		thButton.setLabel("Apply");
		thButton.setActionCommand("Apply");
		thButton.addActionListener(this);
		
		//thSlider = new JSlider();
		//thSlider.setMinimum(0);
		//thSlider.setMaximum(255);
		//thSlider.setOrientation(0);
		thValue = new java.awt.TextField();
		thValue.setText("128");

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

		this.add(countLabel);
		this.add(colorSelect);
		this.add(okButton);
		this.add(cancelButton);
		this.add(clearButton);
		this.add(thValue);
		this.add(thSelect);
		this.add(thButton);
		
		interactionHandler = new PointAnalysisInteractiveHandler(image, restable, this);
	}

	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s == "OK") {
			interactionHandler.analyze();
		} else if (s == "Cancel") {
			remove();
		} else if (s == "Clear") {
			clear();
		} else if (s == "Apply") {
			applyTh();
		}
	}

	private void applyTh() {
		int mode = 2;
		
		if (thSelect.getSelectedItem() == "H")
			mode=0;
		else if (thSelect.getSelectedItem() == "S")
			mode=1;
		else if (thSelect.getSelectedItem() == "B")
			mode=2;
		int th;
		try {
			th = Integer.parseInt(thValue.getText());
		} catch (Exception e) {
			th = 0;
		}
		
		interactionHandler.applyThreshold(mode, th);		
	}

	public void updateCounter(Integer[] count) {
		String s = "";
		for (Integer c : count){
			if (!s.isEmpty())
				s+="|";
			s+=c.toString();
		}
		countLabel.setText(s);
	}

	private void clear() {
		if (confirm("Really clear marks?"))
			interactionHandler.clear();
	}

	public boolean remove() {
		if (confirm("Cancel measurement? This will reset all marks.")) {
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
			interactionHandler.setColor(s);
			interactionHandler.drawOverlay();
		}
	}
}
