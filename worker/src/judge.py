import protoc_build.schema_pb2 as schema


def judge(task):
    result = schema.Result()
    result.success.CopyFrom(schema.Success())

    result.success.duration = 0.5
    result.success.solution.CopyFrom(task.userSubmission.solution)
    result.success.taskId = task.taskId

    return result
