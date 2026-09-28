let students=[], halls=[], exams=[];

const $ = id => document.getElementById(id);

document.querySelectorAll(".tab").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".tab").forEach(b => b.classList.remove("active"));
        document.querySelectorAll(".panel").forEach(p => p.classList.remove("active"));
        btn.classList.add("active");
        $(btn.dataset.tab).classList.add("active");
    });
});

async function api(url, options={}) {
    const response = await fetch(url, {
        headers: {"Content-Type":"application/json", ...(options.headers||{})},
        ...options
    });
    if (!response.ok) {
        let msg = `Request failed (${response.status})`;
        try {
            const body = await response.json();
            msg = body.error || body.message || msg;
            if (body.fields) msg += " " + Object.entries(body.fields).map(([k,v]) => `${k}: ${v}`).join("; ");
        } catch {}
        throw new Error(msg);
    }
    if (response.status === 204) return null;
    return response.json();
}

function showMessage(text, error=false) {
    const box = $("message");
    box.textContent = text;
    box.className = "message" + (error ? " error" : "");
    setTimeout(() => box.classList.add("hidden"), 4500);
}

async function loadAll() {
    try {
        [students,halls,exams] = await Promise.all([
            api("/api/students"), api("/api/halls"), api("/api/exams")
        ]);
        renderStudents(); renderHalls(); renderExams(); renderSelectors();
        const dash = await api("/api/dashboard");
        $("studentCount").textContent = dash.students;
        $("hallCount").textContent = dash.halls;
        $("examCount").textContent = dash.exams;
        $("assignmentCount").textContent = dash.generatedAssignments;
    } catch(e) { showMessage(e.message,true); }
}

function renderStudents() {
    $("studentsTable").innerHTML = students.map(s => `
        <tr><td>${esc(s.registerNumber)}</td><td>${esc(s.name)}</td><td>${esc(s.classSection)}</td>
        <td class="actions"><button onclick="editStudent(${s.id})">Edit</button><button class="danger" onclick="deleteStudent(${s.id})">Delete</button></td></tr>`).join("");
}
function renderHalls() {
    $("hallsTable").innerHTML = halls.map(h => `
        <tr><td>${esc(h.name)}</td><td>${h.rows}</td><td>${h.columns}</td><td>${h.capacity}</td>
        <td class="actions"><button onclick="editHall(${h.id})">Edit</button><button class="danger" onclick="deleteHall(${h.id})">Delete</button></td></tr>`).join("");
}
function renderExams() {
    $("examsTable").innerHTML = exams.map(e => `
        <tr><td>${esc(e.name)}</td><td>${e.examDate}</td><td>${e.startTime} - ${e.endTime}</td>
        <td class="actions"><button onclick="editExam(${e.id})">Edit</button><button class="danger" onclick="deleteExam(${e.id})">Delete</button></td></tr>`).join("");
}
function renderSelectors() {
    $("generateExam").innerHTML = `<option value="">Select exam</option>` + exams.map(e=>`<option value="${e.id}">${esc(e.name)} — ${e.examDate}</option>`).join("");
    $("viewExam").innerHTML = `<option value="">Select exam</option>` + exams.map(e=>`<option value="${e.id}">${esc(e.name)} — ${e.examDate}</option>`).join("");
    $("hallChecks").innerHTML = halls.map(h=>`<label class="check"><input type="checkbox" value="${h.id}"> ${esc(h.name)} (${h.capacity})</label>`).join("");
}

$("studentForm").addEventListener("submit", async e => {
    e.preventDefault();
    const id=$("studentId").value;
    const body={registerNumber:$("regNo").value,name:$("studentName").value,classSection:$("studentSection").value};
    try { await api(id?`/api/students/${id}`:"/api/students",{method:id?"PUT":"POST",body:JSON.stringify(body)});
        showMessage(id?"Student updated.":"Student added."); resetStudentForm(); await loadAll();
    } catch(err){showMessage(err.message,true)}
});
$("hallForm").addEventListener("submit", async e => {
    e.preventDefault();
    const id=$("hallId").value;
    const body={name:$("hallName").value,rows:+$("hallRows").value,columns:+$("hallColumns").value};
    try { await api(id?`/api/halls/${id}`:"/api/halls",{method:id?"PUT":"POST",body:JSON.stringify(body)});
        showMessage(id?"Hall updated.":"Hall added."); resetHallForm(); await loadAll();
    } catch(err){showMessage(err.message,true)}
});
$("examForm").addEventListener("submit", async e => {
    e.preventDefault();
    const id=$("examId").value;
    const body={name:$("examName").value,examDate:$("examDate").value,startTime:$("startTime").value,endTime:$("endTime").value};
    try { await api(id?`/api/exams/${id}`:"/api/exams",{method:id?"PUT":"POST",body:JSON.stringify(body)});
        showMessage(id?"Exam updated.":"Exam added."); resetExamForm(); await loadAll();
    } catch(err){showMessage(err.message,true)}
});

$("generateForm").addEventListener("submit", async e => {
    e.preventDefault();
    const examId=+$("generateExam").value;
    const hallIds=[...document.querySelectorAll("#hallChecks input:checked")].map(x=>+x.value);
    if(!examId || !hallIds.length){showMessage("Select an exam and at least one hall.",true);return}
    try {
        const plan=await api("/api/seating/generate",{method:"POST",body:JSON.stringify({examId,hallIds})});
        $("generationSummary").innerHTML=`<strong>Generated successfully.</strong> ${plan.studentCount} students assigned for ${esc(plan.examName)}.`;
        showMessage("Seating plan generated.");
        await loadAll();
        $("viewExam").value=examId; $("view").classList.add("active"); document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active")); document.querySelector('[data-tab="view"]').classList.add("active");
        renderPlan(plan);
    } catch(err){showMessage(err.message,true)}
});

function editStudent(id){const s=students.find(x=>x.id===id);$("studentId").value=s.id;$("regNo").value=s.registerNumber;$("studentName").value=s.name;$("studentSection").value=s.classSection;focusPanel("students")}
function editHall(id){const h=halls.find(x=>x.id===id);$("hallId").value=h.id;$("hallName").value=h.name;$("hallRows").value=h.rows;$("hallColumns").value=h.columns;focusPanel("halls")}
function editExam(id){const x=exams.find(x=>x.id===id);$("examId").value=x.id;$("examName").value=x.name;$("examDate").value=x.examDate;$("startTime").value=x.startTime;$("endTime").value=x.endTime;focusPanel("exams")}
function focusPanel(id){document.querySelector(`[data-tab="${id}"]`).click()}
function resetStudentForm(){ $("studentId").value="";$("regNo").value="";$("studentName").value="";$("studentSection").value="" }
function resetHallForm(){ $("hallId").value="";$("hallName").value="";$("hallRows").value="5";$("hallColumns").value="6" }
function resetExamForm(){ $("examId").value="";$("examName").value="";$("examDate").value="";$("startTime").value="09:00";$("endTime").value="12:00" }

async function deleteStudent(id){if(!confirm("Delete this student?"))return;try{await api(`/api/students/${id}`,{method:"DELETE"});showMessage("Student deleted.");await loadAll()}catch(e){showMessage(e.message,true)}}
async function deleteHall(id){if(!confirm("Delete this hall?"))return;try{await api(`/api/halls/${id}`,{method:"DELETE"});showMessage("Hall deleted.");await loadAll()}catch(e){showMessage(e.message,true)}}
async function deleteExam(id){if(!confirm("Delete this exam?"))return;try{await api(`/api/exams/${id}`,{method:"DELETE"});showMessage("Exam deleted.");await loadAll()}catch(e){showMessage(e.message,true)}}

async function loadPlan(){
    const id=$("viewExam").value;
    if(!id){$("planOutput").innerHTML="<p class='hint'>Select an exam to view its generated plan.</p>";return}
    try{renderPlan(await api(`/api/seating/exam/${id}`))}catch(e){showMessage(e.message,true)}
}
function renderPlan(plan){
    if(!plan.assignments.length){$("planOutput").innerHTML="<p class='hint'>No seating plan has been generated for this exam.</p>";return}
    const groups={};
    plan.assignments.forEach(a=>(groups[a.hallName]??=[]).push(a));
    $("planOutput").innerHTML=Object.entries(groups).map(([hall,list])=>{
        const maxRow=Math.max(...list.map(x=>x.rowNumber)), maxCol=Math.max(...list.map(x=>x.columnNumber));
        let rows="";
        for(let r=1;r<=maxRow;r++){
            rows+="<tr>";
            for(let c=1;c<=maxCol;c++){
                const a=list.find(x=>x.rowNumber===r&&x.columnNumber===c);
                rows+=a?`<td class="seat"><div>Seat ${a.seatNumber}</div><small>${esc(a.registerNumber)}<br>${esc(a.studentName)}<br>${esc(a.classSection)}</small></td>`:"<td></td>";
            }
            rows+="</tr>";
        }
        return `<div class="hall-plan"><h3>${esc(hall)}</h3><table class="seat-table">${rows}</table></div>`;
    }).join("");
}
function esc(v){return String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]))}

loadAll();
