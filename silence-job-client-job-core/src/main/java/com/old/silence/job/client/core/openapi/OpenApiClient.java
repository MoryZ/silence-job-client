package com.old.silence.job.client.core.openapi;



import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.client.core.dto.JobTriggerDTO;
import com.old.silence.job.client.core.dto.RequestAddOrUpdateJobDTO;
import com.old.silence.job.client.core.dto.RequestUpdateStatusDTO;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Set;

public interface OpenApiClient {
    @Mapping(method = RequestMethod.POST, path = "/api/v1/jobs")
    ApiResult<Object> addJob(RequestAddOrUpdateJobDTO requestAddOrUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/v1/jobs/{id}")
    ApiResult<Object> updateJob(RequestAddOrUpdateJobDTO requestUpdateJobDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/getJobDetail")
    ApiResult<Object> getJobDetail(BigInteger jobId);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerJob")
    ApiResult<Object> triggerJob(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/triggerWorkFlow")
    ApiResult<Object> triggerWorkFlow(JobTriggerDTO jobTriggerDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateJobStatus")
    ApiResult<Object> updateJobStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/updateWorkFlowStatus")
    ApiResult<Object> updateWorkFlowStatus(RequestUpdateStatusDTO statusDTO);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteJob")
    ApiResult<Object> deleteJob(Set<BigInteger> toDeleteIds);

    @Mapping(method = RequestMethod.POST, path = "/api/job/deleteWorkFlow")
    ApiResult<Object> deleteWorkflow(Set<BigInteger> toDeleteIds);
}
