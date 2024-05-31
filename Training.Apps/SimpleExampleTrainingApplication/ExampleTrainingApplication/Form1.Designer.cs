namespace ExampleTrainingApplication
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.button1 = new System.Windows.Forms.Button();
            this.receivedListBox = new System.Windows.Forms.ListBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.sentListBox = new System.Windows.Forms.ListBox();
            this.button2 = new System.Windows.Forms.Button();
            this.sendEventLabel = new System.Windows.Forms.Label();
            this.button3 = new System.Windows.Forms.Button();
            this.clearReceivedListButton = new System.Windows.Forms.Button();
            this.clearSendListButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(118, 12);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(109, 42);
            this.button1.TabIndex = 0;
            this.button1.Text = "button1";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // receivedListBox
            // 
            this.receivedListBox.FormattingEnabled = true;
            this.receivedListBox.HorizontalScrollbar = true;
            this.receivedListBox.Location = new System.Drawing.Point(12, 92);
            this.receivedListBox.Name = "receivedListBox";
            this.receivedListBox.Size = new System.Drawing.Size(267, 420);
            this.receivedListBox.TabIndex = 1;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 73);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(53, 13);
            this.label1.TabIndex = 3;
            this.label1.Text = "Received";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(296, 73);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(29, 13);
            this.label2.TabIndex = 4;
            this.label2.Text = "Sent";
            // 
            // sentListBox
            // 
            this.sentListBox.FormattingEnabled = true;
            this.sentListBox.HorizontalScrollbar = true;
            this.sentListBox.Location = new System.Drawing.Point(299, 92);
            this.sentListBox.Name = "sentListBox";
            this.sentListBox.Size = new System.Drawing.Size(258, 420);
            this.sentListBox.TabIndex = 5;
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(233, 12);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(109, 42);
            this.button2.TabIndex = 6;
            this.button2.Text = "button2";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.button2_Click);
            // 
            // sendEventLabel
            // 
            this.sendEventLabel.AutoSize = true;
            this.sendEventLabel.Location = new System.Drawing.Point(24, 21);
            this.sendEventLabel.Name = "sendEventLabel";
            this.sendEventLabel.Size = new System.Drawing.Size(66, 13);
            this.sendEventLabel.TabIndex = 7;
            this.sendEventLabel.Text = "Send Event:";
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(348, 12);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(109, 42);
            this.button3.TabIndex = 8;
            this.button3.Text = "button3";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.button3_Click);
            // 
            // clearReceivedListButton
            // 
            this.clearReceivedListButton.Location = new System.Drawing.Point(12, 519);
            this.clearReceivedListButton.Name = "clearReceivedListButton";
            this.clearReceivedListButton.Size = new System.Drawing.Size(75, 23);
            this.clearReceivedListButton.TabIndex = 9;
            this.clearReceivedListButton.Text = "Clear";
            this.clearReceivedListButton.UseVisualStyleBackColor = true;
            this.clearReceivedListButton.Click += new System.EventHandler(this.clearReceivedListButton_Click);
            // 
            // clearSendListButton
            // 
            this.clearSendListButton.Location = new System.Drawing.Point(299, 518);
            this.clearSendListButton.Name = "clearSendListButton";
            this.clearSendListButton.Size = new System.Drawing.Size(75, 23);
            this.clearSendListButton.TabIndex = 10;
            this.clearSendListButton.Text = "Clear";
            this.clearSendListButton.UseVisualStyleBackColor = true;
            this.clearSendListButton.Click += new System.EventHandler(this.clearSendListButton_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(569, 554);
            this.Controls.Add(this.clearSendListButton);
            this.Controls.Add(this.clearReceivedListButton);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.sendEventLabel);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.sentListBox);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.receivedListBox);
            this.Controls.Add(this.button1);
            this.Name = "Form1";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Simple Example Training Application";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.ListBox receivedListBox;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.ListBox sentListBox;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label sendEventLabel;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.Button clearReceivedListButton;
        private System.Windows.Forms.Button clearSendListButton;
    }
}

