package br.com.henriqueaurelio.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.henriqueaurelio.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel,HttpServletRequest request){
     var userId = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) userId);

    var currentDate = LocalDateTime.now();
    if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt()))
    {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de ínicio/término deve ser maior que da data atual");
    }

      if(taskModel.getStartAt().isAfter(taskModel.getEndAt()))
    {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data de ínicio deve ser anterior do que a data de término");
    }

    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request){
    var userId = request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser((UUID) userId);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TaskModel taskModel,HttpServletRequest request,@PathVariable UUID id)
  {
    var userId = request.getAttribute("idUser");
    var task = this.taskRepository.findById(id).orElse(null);

    if(task == null)
    {
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada");
    }

    if(!task.getIdUser().equals(userId))
    {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Você precisa ser o dono da tarefa");
    }
    Utils.copyNonNullProperties(taskModel, task);
    var updatedTask = this.taskRepository.save(task);
    return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
  }
}
