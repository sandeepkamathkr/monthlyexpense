// Claude Explain-First Browser Reminder
// Paste this as a bookmark URL or browser extension script

javascript:(function(){
    const reminder = `
🎓 CLAUDE LEARNING MODE ACTIVE

Before Claude makes ANY code changes, it should:
✅ Explain WHAT is being changed
✅ Explain WHY it's necessary  
✅ Explain HOW it integrates with existing code
✅ Explain WHERE it connects to other components
✅ Ask "Does this make sense? Any questions before I implement?"

If Claude doesn't do this automatically, paste this:
"Please explain what you're about to change and why, following the explain-first approach in my CLAUDE.md file."
    `;
    
    alert(reminder.trim());
    
    // Optional: Add visual reminder to page
    const div = document.createElement('div');
    div.style.cssText = `
        position: fixed; 
        top: 10px; 
        right: 10px; 
        background: #007bff; 
        color: white; 
        padding: 10px; 
        border-radius: 5px; 
        z-index: 9999; 
        font-family: Arial; 
        max-width: 300px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
    `;
    div.innerHTML = '🎓 Claude Learning Mode: Explain-First Active<br><small>Click to dismiss</small>';
    div.onclick = () => div.remove();
    document.body.appendChild(div);
    
    setTimeout(() => div && div.remove(), 10000); // Auto-remove after 10 seconds
})();